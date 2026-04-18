package fitwell.domain.user;

import fitwell.domain.shared.PreferredUpdateMethod;

public class Trainee {
    private Integer id; // AutoNumber
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String password;
    private PreferredUpdateMethod preferredUpdateMethod;

    public Trainee(Integer id, String firstName, String lastName, String phone, String email, String preferredUpdateMethod) {
        this(id, firstName, lastName, phone, email, "", PreferredUpdateMethod.fromValue(preferredUpdateMethod));
    }

    public Trainee(Integer id, String firstName, String lastName, String phone, String email, PreferredUpdateMethod preferredUpdateMethod) {
        this(id, firstName, lastName, phone, email, "", preferredUpdateMethod);
    }

    public Trainee(Integer id, String firstName, String lastName, String phone, String email, String password, PreferredUpdateMethod preferredUpdateMethod) {
        this.id = id;
        this.firstName = safe(firstName);
        this.lastName = safe(lastName);
        this.phone = safe(phone);
        this.email = safe(email);
        this.password = safe(password);
        this.preferredUpdateMethod = preferredUpdateMethod == null ? PreferredUpdateMethod.EMAIL : preferredUpdateMethod;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = safe(password); }
    public String getPreferredUpdateMethod() { return preferredUpdateMethod.name(); }
    public PreferredUpdateMethod getPreferredUpdateMethodEnum() { return preferredUpdateMethod; }

    public String fullName() { return (firstName + " " + lastName).trim(); }

    public void updateProfile(String firstName, String lastName, String phone, String email, PreferredUpdateMethod preferredUpdateMethod) {
        this.firstName = safe(firstName);
        this.lastName = safe(lastName);
        this.phone = safe(phone);
        this.email = safe(email);
        this.preferredUpdateMethod = preferredUpdateMethod == null ? PreferredUpdateMethod.EMAIL : preferredUpdateMethod;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
