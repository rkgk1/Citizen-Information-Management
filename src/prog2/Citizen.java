package prog2.finalgroup;

public class Citizen implements Comparable<Citizen> {
    private String fullName;
    private String email;
    private String address;
    private int age;
    private boolean resident;
    private int district;
    private char gender;

    public Citizen(String fullName, String email, String address, int age, boolean resident, int district, char gender) {
        this.fullName = fullName;
        this.email = email;
        this.address = address.replace("\"", ""); // Remove double quotes if present
        this.age = age;
        this.resident = resident;
        this.district = district;
        this.gender = gender;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public int getAge() {
        return age;
    }

    public boolean isResident() {
        return resident;
    }

    public int getDistrict() {
        return district;
    }

    public char getGender() {
        return gender;
    }

    @Override
    public int compareTo(Citizen other) {
        return this.fullName.compareTo(other.fullName);
    }

    @Override
    public String toString() {
        return String.format("Name: %s, Email: %s, Address: %s, Age: %d, Resident: %b, District: %d, Gender: %c",
                fullName, email, address, age, resident, district, gender);
    }

    public Object[] toRowData() {
        String residentType = resident ? "Resident" : "Non-Resident";
        return new Object[]{fullName, address, age, district, residentType, gender};
    }
}
