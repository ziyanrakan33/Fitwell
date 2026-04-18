package fitwell.entity;

public class Consultant {
    private final int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String password;
    private final boolean approved;
    private ConsultantRole role;

    public Consultant(int id, String firstName, String lastName, String phone, String email) {
        this(id, firstName, lastName, phone, email, "", true, ConsultantRole.MANAGER);
    }

    public Consultant(int id, String firstName, String lastName, String phone, String email, String password) {
        this(id, firstName, lastName, phone, email, password, true, ConsultantRole.MANAGER);
    }

    public Consultant(int id, String firstName, String lastName, String phone, String email, String password, boolean approved) {
        this(id, firstName, lastName, phone, email, password, approved, ConsultantRole.MANAGER);
    }

    public Consultant(int id, String firstName, String lastName, String phone, String email, String password, boolean approved, ConsultantRole role) {
        this.id = id;
        this.firstName = firstName == null ? "" : firstName;
        this.lastName = lastName == null ? "" : lastName;
        this.phone = phone == null ? "" : phone;
        this.email = email == null ? "" : email;
        this.password = password == null ? "" : password;
        this.approved = approved;
        this.role = role == null ? ConsultantRole.MANAGER : role;
    }

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public boolean isApproved() { return approved; }
    public ConsultantRole getRole() { return role; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(ConsultantRole role) { this.role = role; }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + role.name() + ")";
    }
}
