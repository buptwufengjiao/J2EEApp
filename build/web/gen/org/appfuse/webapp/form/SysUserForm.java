package org.appfuse.webapp.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;

/**
 * Generated by XDoclet/actionform. This class can be further processed with XDoclet/webdoclet/strutsconfigxml and XDoclet/webdoclet/strutsvalidationxml.
 *
 * @struts.form name="sysUserForm" 
 */
public class SysUserForm
    extends    BaseForm
    implements java.io.Serializable
{

    protected String id;

    protected String userName;

    protected RoleAppForm roleApp = new RoleAppForm();

    protected String pass;

    protected String realName;

    protected String phone;

    protected String email;

    protected String rfidLabel;

    /** Default empty constructor. */
    public SysUserForm() {}

    public String getId()
    {
        return this.id;
    }
   /**
    */

    public void setId( String id )
    {
        this.id = id;
    }

    public String getUserName()
    {
        return this.userName;
    }
   /**
    * @struts.validator type="required"
    */

    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    public RoleAppForm getRoleAppForm()
    {
        return this.roleApp;
    }
   /**
    * @struts.validator type="required"
    * @struts.validator
    */

    public void setRoleAppForm( RoleAppForm roleApp )
    {
        this.roleApp = roleApp;
    }

    /** 
     *  Getter/Setter pair so BeanUtil.copyProperties(dest, orig) will work 
     *  Any properties modified in the web tier should use the get/setRoleAppForm 
     *  methods.
     */
    public org.appfuse.model.RoleApp getRoleApp() throws Exception
    {
        return (org.appfuse.model.RoleApp) org.appfuse.util.ConvertUtil.convert(this.roleApp);
    }

    public void setRoleApp(org.appfuse.model.RoleApp roleApp) throws Exception
    {
        setRoleAppForm((RoleAppForm) org.appfuse.util.ConvertUtil.convert(roleApp));
    }

    public String getPass()
    {
        return this.pass;
    }
   /**
    * @struts.validator type="required"
    */

    public void setPass( String pass )
    {
        this.pass = pass;
    }

    public String getRealName()
    {
        return this.realName;
    }
   /**
    * @struts.validator type="required"
    */

    public void setRealName( String realName )
    {
        this.realName = realName;
    }

    public String getPhone()
    {
        return this.phone;
    }
   /**
    */

    public void setPhone( String phone )
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return this.email;
    }
   /**
    */

    public void setEmail( String email )
    {
        this.email = email;
    }

    public String getRfidLabel()
    {
        return this.rfidLabel;
    }
   /**
    */

    public void setRfidLabel( String rfidLabel )
    {
        this.rfidLabel = rfidLabel;
    }

        /* To add non XDoclet-generated methods, create a file named
           actionform-methods-SysUserForm.java
           containing the additional code and place it in your merge dir.
        */
    /**
     * @see org.apache.struts.action.ActionForm#reset(org.apache.struts.action.ActionMapping,
     *                                                javax.servlet.http.HttpServletRequest)
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        // reset any boolean data types to false

    }

}
