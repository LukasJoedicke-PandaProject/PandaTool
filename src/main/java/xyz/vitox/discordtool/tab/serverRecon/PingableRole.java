package xyz.vitox.discordtool.tab.serverRecon;

public class PingableRole {

    public String roleName;
    public String roleID;

    public PingableRole(String roleName, String roleID) {
        this.roleName = roleName;
        this.roleID = roleID;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleID() {
        return roleID;
    }

    public void setRoleID(String roleID) {
        this.roleID = roleID;
    }
}
