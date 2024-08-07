/* Dimitris Karatzas icsd13072
   Nikolaos Katsiopis icsd13076
   Christos Papakostas icsd13143
 */

package sec3.domain;

import java.io.Serializable;

public class User implements Serializable {
    private String name, lname, username, salt;
    private final byte[] encrypted_password;
    public User(String user, byte[] pass, String nam, String lnam, String sal) {
        this.username = user;
        this.encrypted_password = new byte[pass.length];
        System.arraycopy(pass, 0, encrypted_password, 0, pass.length);
        this.name = nam;
        this.lname = lnam;
        this.salt = sal;
    }
    public String getUsername() {
        return username;
    }
    public byte[] getPassword() {
        return encrypted_password;
    }
    public String getName() {
        return name;
    }
    public String getLname() {
        return lname;
    }
    public String getSalt() {
        return salt;
    }
    public void setUsername(String user) {
        username = user;
    }
    public void setPassword(byte[] pass) {
        System.arraycopy(pass, 0, encrypted_password, 0, pass.length);
    }
    public void setName(String nam) {
        name = nam;
    }
    public void setLname(String lnam) {
        lname = lnam;
    }
    public void setSalt(String sal) {
        this.salt = sal;
    }
}
