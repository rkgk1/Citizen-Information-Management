package prog2.finalgroup;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MyProgramUtility {

    public static List<Citizen> readCitizensFromFile(String fileName) {
        List<Citizen> citizens = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|([^,]+)");

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                List<String> fields = new ArrayList<>();
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    if (matcher.group(1) != null) {
                        fields.add(matcher.group(1));
                    } else {
                        fields.add(matcher.group(2));
                    }
                }

                if (fields.size() == 8) {
                    try {
                        String firstName = fields.get(0);
                        String lastName = fields.get(1);
                        String fullName = firstName + " " + lastName;
                        String email = fields.get(2);
                        String address = fields.get(3);
                        int age = Integer.parseInt(fields.get(4).trim());
                        boolean resident = fields.get(5).trim().equals("Resident");
                        int district = Integer.parseInt(fields.get(6).trim());
                        char gender = fields.get(7).trim().charAt(0);

                        citizens.add(new Citizen(fullName, email, address, age, resident, district, gender));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing number: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return citizens;
    }

    public static void writeCitizensToFile(List<Citizen> citizens, String fileName) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (Citizen citizen : citizens) {
                String residentType = citizen.isResident() ? "Resident" : "Non-Resident";
                String[] names = citizen.getFullName().split(" ", 2);
                String firstName = names.length > 1 ? names[0] : "";
                String lastName = names.length > 1 ? names[1] : "";
                bw.write(String.format("%s,%s,%s,\"%s\",%d,%s,%d,%c%n",
                        firstName, lastName, citizen.getEmail(), citizen.getAddress(),
                        citizen.getAge(), residentType, citizen.getDistrict(), citizen.getGender()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long countResidents(List<Citizen> citizens) {
        return citizens.stream().filter(Citizen::isResident).count();
    }

    public static long countNonResidents(List<Citizen> citizens) {
        return citizens.stream().filter(c -> !c.isResident()).count();
    }

    public static long countByGender(List<Citizen> citizens, char gender) {
        return citizens.stream().filter(c -> c.getGender() == gender).count();
    }

    public static List<Citizen> filterByDistrict(List<Citizen> citizens, int district) {
        return citizens.stream().filter(c -> c.getDistrict() == district).collect(Collectors.toList());
    }

    public static List<Citizen> sortByAge(List<Citizen> citizens) {
        return citizens.stream().sorted((c1, c2) -> Integer.compare(c1.getAge(), c2.getAge())).collect(Collectors.toList());
    }
}
