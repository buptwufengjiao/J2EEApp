package org.appfuse.webapp.action;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.mlw.vlh.ValueList;
import net.mlw.vlh.ValueListHandler;
import net.mlw.vlh.ValueListInfo;
import net.mlw.vlh.web.mvc.ValueListHandlerHelper;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.appfuse.Constants;



import org.appfuse.model.Goods;





import org.appfuse.service.GoodsManager;





import org.appfuse.webapp.form.GoodsForm;
import org.appfuse.webapp.util.NowDate;
import org.appfuse.webapp.util.SearchMap;
import org.springframework.beans.BeansException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;



/**
 * 
 * @author ycs
 * @struts.action name="goodsForm" path="/goods" scope="request"
 *  validate="false" parameter="method" 
 *   
 *  
 * @struts.action name="goodsForm" path="/goodss" scope="request"
 * validate="false" parameter="method" input="mainMenu"
 * @struts.action name="goodsForm" path="/editgoods" scope="request"
 *  validate="false" parameter="method" input="list"
 * @struts.action name="goodsForm" path="/savegoods" scope="request"
 *  validate="true" parameter="method" input="edit"
 *  
 *  
 *  
 *  
 *  
 * @struts.action-forward name="edit" path="/WEB-INF/pages/basedata/goodsForm.jsp"
 * @struts.action-forward name="view" path="/WEB-INF/pages/basedata/goodsView.jsp"
 * @struts.action-forward name="find" path="/WEB-INF/pages/basedata/goodsFind.jsp"
  * @struts.action-forward name="list" path="/WEB-INF/pages/basedata/goodsList.jsp"
 */

public class GoodsAction extends BaseAction {
	 public ActionForward cancel(ActionMapping mapping, ActionForm form,
             HttpServletRequest request,
             HttpServletResponse response)
throws Exception {
    GoodsForm goodsForm = (GoodsForm) form;

    resetGoods(goodsForm); //FormBean复位

return gotoFind(mapping, form, request, response); //进入查询页面
}
	
	
     /**
		 * 删除方法
		 * 
		 * @param mapping
		 * @param form
		 * @param request
		 * @param response
		 * @return
		 * @throws Exception
		 */
	    public ActionForward delete(ActionMapping mapping, ActionForm form,
	                                HttpServletRequest request,
	                                HttpServletResponse response)
	    throws Exception {
			//HttpSession session = null;

	    	GoodsManager mgr = (GoodsManager) getBean("goodsManager");

			ActionMessages messages = new ActionMessages();

			//BzForm bzForm = (BzForm) form;

			//String id = null;
			//获取删除的来源，如果是"del_many"则是从list中来的直接批量删除
			//如果为del_many_one,则是批量修改中的单条修改删除,或者批量修改中的查看时候的删除
			//如果为del,则为单条查看,或者单条修改删除
			String type = getType(request);

			if ("del_many".equals(type)) { //如果是多条删除
				return delMany(mapping, form, request, response, mgr, messages,
						type);

			} else { //如果单条删除
				return delOne(mapping, form, request, response, mgr,
						messages, type);

			}

	    }
		/**
		 * 删除多条
		 * @param mapping
		 * @param form
		 * @param request
		 * @param response
		 * @param mgr
		 * @param messages
		 * @param type
		 * @return
		 * @throws Exception
		 */
		private ActionForward delMany(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response,
				GoodsManager mgr, ActionMessages messages, String type)
				throws Exception {
			//防止重复提交
			if (!isTokenValid(request)) {
				resetGoods((GoodsForm)form);
				saveToken(request);
			} else {
				resetToken(request);
				String[] sel_ids = request.getParameterValues(Constants.IDS);

				if (sel_ids != null) { //如果是删除多条，查询结果
					for (int i = 0; i < sel_ids.length; i++) {
						String id = sel_ids[i];
						try{
							mgr.removeGoods(id); //删除一条记录
						} catch (RuntimeException e) {
							e.printStackTrace();
						}
					}

				}
			}
			return goToSearch(mapping, form, request, response, messages); //调转到Search方法
		}

		/**
		 * 删除一条记录
		 * @param mapping
		 * @param form
		 * @param request
		 * @param response
		 * @param sel_ids
		 * @param mgr
		 * @param messages
		 * @param type
		 * @return
		 * @throws Exception
		 */
		private ActionForward delOne(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response,
				GoodsManager mgr, ActionMessages messages,
				String type) throws Exception {
			HttpSession session = request.getSession(); //得到Session，从Session中查找是否有未处理的id
			String id = null;
			//防止重复提交
			if (!isTokenValid(request)) {  
				resetGoods((GoodsForm)form);//如果已经执行过了,则跳入本部分
				saveToken(request);
			} else {
				resetToken(request);
				id = request.getParameter("id");
				try{
					mgr.removeGoods(id);  //删除记录
				} catch (RuntimeException e) {
					getError(session, e, "error.goods.deleted", id);
				}
			}

			if (Constants.DEL.equals(type)) { //如果是删除普通的单条记录,跳转到查询结果页面
				saveMsg(request, messages, ".deleted");
				return search(mapping, form, request, response);
			} else {  //如果是批量修改的修改或者查看页面的单条删除
				String[] sel_ids = (String[])session.getAttribute(Constants.SEL_IDS);  //获取Session中的批量选中的id的数组

				int pos = ((Integer) session.getAttribute(Constants.POS))
						.intValue(); 
				if (sel_ids != null && pos >= sel_ids.length - 1) { //如果为最后一条记录
					decPos(session, pos);   //将pos减1
				}
				sel_ids = delID(id, session, sel_ids);//删除SEL_IDS数组中的当前的id

				//获取删除后的上一条、下一条的图标
				return getDelImgState(mapping, form, request, response, messages,
						session, sel_ids);

			}

		}
		/**
		 * 根据条件返回查询结果
		 * 
		 * @param mapping
		 * @param form
		 * @param request
		 * @param response
		 * @return
		 * @throws Exception
		 */
		public ActionForward search(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response)
				throws Exception {
			String list_goods = Constants.LIST_GOODS;
			String goodsID = Constants.goodsID;
			return do_Search(mapping, request, list_goods, goodsID);
		}
		
		public ActionForward do_Search(ActionMapping mapping, HttpServletRequest request, String list_goods, String goodsID) throws BeansException {
			String type = getType(request);

			HttpSession session = request.getSession();

			//得到查询时的form信息
			GoodsForm goods ;
			try {
				goods = (GoodsForm) session.getAttribute("form");
			} catch (RuntimeException e) {
				e.printStackTrace();
				goods = null;
				 return do_SearchError(mapping);
			}
			//得到ValueList相关的HandlerHelper
			ValueListHandlerHelper vlHelper = getValueListHelper();

			WebApplicationContext context = WebApplicationContextUtils
					.getWebApplicationContext(getServlet().getServletContext());
			ValueListHandler vlh = (ValueListHandler) context.getBean(
					"valueListHandler", ValueListHandler.class);

			Map map = new HashMap();

			//从valueListInfo中得到当前页码号！
			ValueListInfo oldInfo = vlHelper.getValueListInfo(request,
					Constants.goodsID);
			//获取从第几页跳转来的
			int pageno = oldInfo.getPagingPage();

			map = SearchMap.getGoodsMap(request, goods); //得到map

			ValueListInfo info = new ValueListInfo(map);
			//设置显示到第几页
			if (!(Constants.DEL_MANY.equals(type) || (Constants.DEL.equals(type) || Constants.DEl_MANY_ONE.equals(type)))) {
				info.setPagingPage(pageno);
			}

			ValueList valueList = vlh.getValueList(list_goods, info);
			vlHelper.backupAndSet(request, valueList, list_goods, goodsID);

			//解决重复提交的问题
			saveToken(request);

			return mapping.findForward("list");
		}

		
		
		/**
		 * edit方法，如果是新建，则进入一个空form，
		 * 否则，进入修改form
		 * 
		 * @param mapping
		 * @param form
		 * @param request
		 * @param response
		 * @return
		 * @throws Exception
		 */

	    public ActionForward edit(ActionMapping mapping, ActionForm form,
	                              HttpServletRequest request,
	                              HttpServletResponse response)
	    throws Exception {

	        //防止重复提交
			saveToken(request);

			String type = getType(request);

			HttpSession session = request.getSession();
			String id = null;

			String img_state = null;
			GoodsForm goodsForm = (GoodsForm) form;

			boolean isNew = ("".equals(goodsForm.getGoodsID()) || goodsForm.getGoodsID() == null);

			if (isNew) { //当选择新建时，删除图标为灰色，功能不可使用
				//清楚Session中的SEL_IDS
				session.removeAttribute(Constants.SEL_IDS);

				img_state = Constants.NONE; //当选择新建时，没有上一条、下一条图标

				request.setAttribute(Constants.IMG_STATE, img_state);
				return mapping.findForward("edit");
			} else {
				//1.从每行的修改图标来的修改，传入id,getParameter();
				//2.从View页面来的修改,传入id,getParameter();
				//3.批量修改，getAttribute(POS);
				if (type.equals(Constants.EDIT_MANY)
						|| type.equals(Constants.DEL_MANY)||type.equals(Constants.DEl_MANY_ONE)) { //如果从LIST传过来的，则肯定是批量修改
					String[] sel_ids = (String[]) session
							.getAttribute(Constants.SEL_IDS); //获取id的数组

					Integer pos = (Integer) session.getAttribute(Constants.POS); //获取当前位置
					if (sel_ids.length > 0 && pos.intValue() <= sel_ids.length){
						id = sel_ids[pos.intValue()]; //当前id
					}else{
						return search(mapping, form, request, response);
					}

				} else {
					//如果从每行的图标传过来
					//如果是从view页面转过来的
					img_state = Constants.NONE;
					request.setAttribute(Constants.IMG_STATE, img_state);

					id = request.getParameter("id"); //首先尝试从request中获取id
				}

				GoodsManager mgr = (GoodsManager) getBean("goodsManager");
				 try {
	                getGoods(mapping, request, id, mgr);
	            } catch (RuntimeException e) {
	                getError(session, e, "error.goods.selected", id);

	                return search(mapping, form, request, response);
	            }

				if (type.equals(Constants.VIEW)) { //如果是从VIEW来的
					return mapping.findForward("view");
				} else { //如果是从EDIT来的
					return mapping.findForward("edit");
				}

			}

	    }
		public ActionForward edit_many(ActionMapping mapping, ActionForm form,
				HttpServletRequest request, HttpServletResponse response)
				throws Exception {
			saveToken(request);  //防止重复提交
			//String type = getType(request); //得到type

			String id = null;

			//String img_state = null; //上一条、下一条图标的显示标志
			Integer pos = new Integer(0); //id数组的位置的初始化

			//取得选择的id的数组，并将其连同当前id存入session中
			HttpSession session = request.getSession();

			//BzForm bzForm = (BzForm) form;

			//取得多条记录id数组！
			String[] sel_ids = request.getParameterValues(Constants.IDS);//获取提交的id的数组

			if(sel_ids == null){
			    sel_ids = (String[])session.getAttribute(Constants.SEL_IDS); //取得选择的ID数组
			    pos = (Integer)session.getAttribute(Constants.POS);    //取得数组的位置
			}

			if (sel_ids == null) { //如果是多条批量修改，然后从form中点击“修改”按钮
				id = request.getParameter("id"); //
			} else {
				//如果提交的id为0个，则跳转到查询页面
				if (sel_ids.length == 0) {
					return goToSearch(mapping, form, request, response, null);
				} else {
					//将得到的id数组|当前记录在数组中的位置存入session
					session.setAttribute(Constants.SEL_IDS, sel_ids);
					session.setAttribute(Constants.POS, pos);
					id = sel_ids[pos.intValue()]; //取得当前索引的id，其实是第一条id，索引为0
				}
			}

			if (id == null) { //如果用户没选择id,同时浏览器关闭了javascript的功能
				return search(mapping, form, request, response);
			} else {
				GoodsManager mgr = (GoodsManager) getBean("goodsManager");
	            try {
	                getGoods(mapping, request, id, mgr); //更新FormBean
	            } catch (RuntimeException e) {
	                getError(session, e, "error.goods.selected", id);

	                return search(mapping, form, request, response);
	            }
				setImgState(request, pos.intValue(), sel_ids);
				return mapping.findForward("edit"); //经过这里的跳转，进入edit方法。
			}
		}

		/**
		 * 批量翻页
		 * 
		 * @param mapping
		 * @param request
		 * @param session
		 * @param i
		 * @return
		 * @throws Exception
		 */
		public ActionForward batch_ChangeID(ActionMapping mapping, ActionForm form,
	            HttpServletRequest request, HttpServletResponse response,
	            HttpSession session, int i)
				throws Exception {
		    String[] sel_ids = (String[]) session.getAttribute(Constants.SEL_IDS); //从Session中获取选择的id的数组

			Integer pos = new Integer(i); //当前位置的索引，Session中存储的id数组的当前记录位置

			session.setAttribute(Constants.POS, pos); //把当前位置记录到session中

			String id;
			try {
	            id = sel_ids[i]; //获取当前id
	        } catch (RuntimeException e) {
	            e.printStackTrace();
	            return search(mapping, form, request, response);
	        }

			GoodsManager mgr = (GoodsManager) getBean("goodsManager");
	        try {
	            getGoods(mapping, request, id, mgr);	
	        } catch (RuntimeException e1) {
	            getError(session, e1, "error.goods.selected", id);
	        }
			setImgState(request, i, sel_ids);

			return mapping.findForward("edit");
		}


	    
	    
	    
	    
	    
	        /**
		 * 执行search
		 * @param mapping
		 * @param request
		 * @param bzlist
		 * @param bzid
		 * @return
		 * @throws BeansException
		 */
	 
	 
	
	
    public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Entering 'add' method");
		}
		// add else do
		Goods goods = new Goods();
		GoodsForm goodfrm=(GoodsForm) convert(goods);
		updateFormBean(mapping, request, goodfrm);

		return mapping.findForward("edit");
	}
    
    public ActionForward save(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
    	
    	Goods goods = new Goods();
      	GoodsForm frmgoods =( GoodsForm)form;
      	frmgoods.setGoodsID("");
      	goods=(Goods)convert(frmgoods);
       	
    	GoodsManager mgr=(GoodsManager)getBean("goodsManager");
        try { 
        	mgr.saveGoods(goods);
     //   	System.out.print(l);
        }catch(Exception e){
        	e.printStackTrace();
        	
        }
    	
    	return mapping.findForward("view");
    	
    }
    	
   
    
    public ActionForward gotoFind(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Entering 'find' method");
		}
		// add else do
		Goods goods = new Goods();
		GoodsForm goodfrm=(GoodsForm) convert(goods);
		updateFormBean(mapping, request, goodfrm);

		return mapping.findForward("find");
	}
    
    public ActionForward find(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		HttpSession session = request.getSession();
		sessionInfo(request, session); 	
		
    	GoodsForm frmgoods =( GoodsForm)form;
    	
    	session.setAttribute(Constants.FORM, frmgoods);
    	
    	ValueListHandler vlh = getValueListHandler();
		Map map = new HashMap();
		
		map = SearchMap.getGoodsMap(request, frmgoods);
    	
		ValueListHandlerHelper vlHelper = getValueListHelper();
		ValueList valueList = vlh.getValueList(Constants.LIST_GOODS, new ValueListInfo(map));
		vlHelper.backupAndSet(request, valueList, Constants.LIST_GOODS, Constants.goodsID);
		
    	return mapping.findForward("list");
    }
    
    /**
	 * 将bzForm所有字段复位
	 * @param bzForm
	 * 
	 */
	private void resetGoods(GoodsForm goodsForm) {
		Goods goods = new Goods();
		//此处适用了Apache的BeanUtils类的copyProperties方法,该方法第一个参数是目标对象，后面的参数是源对象
		try {
			BeanUtils.copyProperties(goodsForm, goods);
		} catch (IllegalAccessException e1) {
			log.debug("resetGoods 出错-IllegalAccessException");
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			log.debug("resetGoods 出错-InvocationTargetException");
			e1.printStackTrace();
		}
	}
	
	public ActionForward getDelImgState(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response, ActionMessages messages,
			HttpSession session, String[] sel_ids) throws Exception {

		//String id;
		if (sel_ids == null || sel_ids.length == 0) { //如果选择的ID的数组为null或者数组长度为0，则说明没有上一条、下一条记录了，则进入Search()方法。
			return goToSearch(mapping, form, request, response, messages);
		} else {
			//String img_state = null; //记录图标的状态，显示上一页、下一页、或者都显示
			int pos = ((Integer) session.getAttribute(Constants.POS))
					.intValue(); //当前id在数组中的索引(位置),该位置在批量修改进入时写入为0,在进入单条修改页面后，则修改该数值。

			return com_edit(mapping, form, request, response, messages,
					session, sel_ids, pos);
		}
	}
	public ActionForward com_edit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			ActionMessages messages, HttpSession session, String[] sel_ids,
			int pos) throws Exception {
		String img_state;
		//然后将Session中的POS的位置更新为当前的位置.
		session.setAttribute(Constants.POS, new Integer(pos));

		img_state = getImgState(pos, sel_ids); //获取上一条、下一条图标的状态

		request.setAttribute(Constants.IMG_STATE, img_state);

		//跳转到指定id的修改页面
		return goToEdit(mapping, form, request, response, messages);
	}
	/**
	 * 跳转到Edit方法，跳转前，做一些处理
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param messages
	 * @return
	 * @throws Exception
	 */
	public ActionForward goToEdit(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			ActionMessages messages) throws Exception {
		saveMsg(request, messages, "goods.deleted");

		return edit(mapping, form, request, response);
	}
	
	
	
    /**
* @param mapping
* @param request
* @param id
* @param mgr
* @throws Exception
*/
private void getGoods(ActionMapping mapping, HttpServletRequest request, String id, GoodsManager mgr) throws Exception {
   GoodsForm goodsForm;
   Goods goods;
   goods = mgr.getGood(id);
   goodsForm = (GoodsForm) convert(goods);
   updateFormBean(mapping, request, goodsForm);

}

public ActionForward do_find(ActionMapping mapping, ActionForm form, HttpServletRequest request, String goodslist, String goodsid) throws BeansException {
	HttpSession session = request.getSession();
	//清除Session中存有的信息
	session.removeAttribute(Constants.SEL_IDS);
	//String type = getType(request);

	GoodsForm goods = (GoodsForm) form;
	//备份与字段相关的form信息
	session.setAttribute("form",goods);

	//获取ValueList相关的东西
	WebApplicationContext context = WebApplicationContextUtils
			.getWebApplicationContext(getServlet().getServletContext());
	ValueListHandler vlh = (ValueListHandler) context.getBean(
			"valueListHandler", ValueListHandler.class);

	Map map = new HashMap();


	map = SearchMap.getGoodsMap(request, goods); //得到map //getMap(request, bz); //得到条件Map

	ValueListHandlerHelper vlHelper = getValueListHelper();
	ValueList valueList = vlh.getValueList(goodslist, new ValueListInfo(
			map));
	vlHelper.backupAndSet(request, valueList, goodslist, goodsid);

	//导出特定参数,从而完成Excel文件
	String excelFileName = "goods查询结果.xls"; //注意：要修改该文件名
	String listname = goodslist;

	setExcelInfo(request, valueList, excelFileName, listname);

	return mapping.findForward("list");
}

/**
*如果未指定method，则调用该方法
*/
public ActionForward unspecified(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response)
		throws Exception {
	return gotoFind(mapping, form, request, response);
}

    //李铁峰添加的方法
/**
 * 获取在修改状态下的上一条、下一条图标是否显示
 * 
 * @param request
 * @return
 */
public String getUptImgState(HttpServletRequest request,
		HttpSession session) {
	//如果从单条记录修改，然后保存，则不显示上一条、下一条图标

	//否则，要判断是否还有下一条，如果有，显示下一条图标，否则，不显示下一条图标
	//判断是否是第一条记录，如果是，则不显示上一条图标
	String img_state = null;
	String type = getType(request); //得到type

	if ("save".equals(type)) {
		img_state = Constants.NONE;
	} else {
		String[] sel_ids = (String[]) session
				.getAttribute(Constants.SEL_IDS);

		Integer pos = (Integer) session.getAttribute(Constants.POS);

		int i = pos.intValue();

		img_state = getImgState(i, sel_ids); //获取上一条、下一条的图标的状态
	}
	return img_state;
}
/**
 * 获取from的值
 * @param request
 * @return
 */
/**
public String getType(HttpServletRequest request) {
	String type = request.getParameter(Constants.FROM);

	if (type == null) {
		type = "";
	}

	return type;
}
*/

//编辑下一条记录
public ActionForward next(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response)
		throws Exception {

	HttpSession session = request.getSession();
	Integer pos = (Integer) session.getAttribute(Constants.POS);

	int i = pos.intValue() + 1; //得到当前id的pos的值

	return batch_ChangeID(mapping, form, request, response, session, i);
}

//编辑上一条记录！
public ActionForward prior(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response)
		throws Exception {

	HttpSession session = request.getSession();
	Integer pos = (Integer) session.getAttribute(Constants.POS);

	int i = pos.intValue() - 1;

	return batch_ChangeID(mapping, form, request, response, session, i);
}




	
	
	/**
	 * 跳转到search方法，跳转前，做一些处理
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @param messages
	 * @return
	 * @throws Exception
	 */
	public ActionForward goToSearch(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response,
			ActionMessages messages) throws Exception {
		saveMsg(request, messages, "goods.deleted");

		return search(mapping, form, request, response);
	}

    
}
