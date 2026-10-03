package com.medbook.config;

import com.medbook.entity.Doctor;
import com.medbook.entity.User;
import com.medbook.repository.UserRepository;
import com.medbook.service.DoctorService;
import com.medbook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create admin user
        createAdminUser();
        // Create sample doctors
        createSampleDoctors();
    }

    private void createAdminUser() {
        try {
            System.out.println("🔍 Checking for existing admin user...");
            
            // Check if admin already exists
            if (userRepository.findByEmail("admin@medbook.com").isPresent()) {
                System.out.println("✅ Admin user already exists, skipping creation");
                return;
            }

            System.out.println("🚀 Creating admin user...");
            
            // Create Admin User
            User adminUser = new User();
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setEmail("admin@medbook.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setConfirmPassword("admin123"); // Set confirmPassword to pass validation
            adminUser.setPhoneNumber("+27 11 000 0000");
            adminUser.setRole(User.Role.ADMIN);
            
            // Save directly to repository to avoid email sending issues
            User savedAdmin = userRepository.save(adminUser);
            
            System.out.println("✅ Admin user created successfully!");
            System.out.println("   📧 Email: admin@medbook.com");
            System.out.println("   🔑 Password: admin123");
            System.out.println("   👤 Role: ADMIN");
            System.out.println("   🆔 ID: " + savedAdmin.getId());
            System.out.println("   🔐 Encoded Password: " + savedAdmin.getPassword());
            
        } catch (Exception e) {
            System.err.println("❌ Failed to create admin user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSampleDoctors() {
        // Check if doctors already exist - if they do, skip creation
        if (!doctorService.getAllDoctors().isEmpty()) {
            System.out.println("Doctors already exist, skipping creation");
            return;
        }

        // Create Doctor 1 - Cardiologist
        User doctor1User = new User();
        doctor1User.setFirstName("John");
        doctor1User.setLastName("Smith");
        doctor1User.setEmail("dr.smith@medbook.com");
        doctor1User.setPassword("password123");
        doctor1User.setPhoneNumber("+27 72 123 4567");
        doctor1User.setRole(User.Role.DOCTOR);
        userService.registerUser(doctor1User);

        Doctor doctor1 = new Doctor(doctor1User, "Cardiology", "MD123456", 
                                 "Experienced cardiologist with 15 years of practice. Specializes in heart conditions, hypertension, and cardiac procedures.", 15, "Cardiology Center");
        doctorService.updateDoctor(doctor1);

        // Create Doctor 2 - Neurologist
        User doctor2User = new User();
        doctor2User.setFirstName("Sarah");
        doctor2User.setLastName("Johnson");
        doctor2User.setEmail("dr.johnson@medbook.com");
        doctor2User.setPassword("password123");
        doctor2User.setPhoneNumber("+27 72 234 5678");
        doctor2User.setRole(User.Role.DOCTOR);
        userService.registerUser(doctor2User);

        Doctor doctor2 = new Doctor(doctor2User, "Neurology", "MD234567", 
                                 "Board-certified neurologist specializing in brain disorders, epilepsy, and neurological conditions.", 12, "Neurology Clinic");
        doctorService.updateDoctor(doctor2);

        // Create Doctor 3 - Orthopedist
        User doctor3User = new User();
        doctor3User.setFirstName("Michael");
        doctor3User.setLastName("Brown");
        doctor3User.setEmail("dr.brown@medbook.com");
        doctor3User.setPassword("password123");
        doctor3User.setPhoneNumber("+27 72 345 6789");
        doctor3User.setRole(User.Role.DOCTOR);
        userService.registerUser(doctor3User);

        Doctor doctor3 = new Doctor(doctor3User, "Orthopedics", "MD345678", 
                                 "Orthopedic surgeon with expertise in joint replacement, sports injuries, and bone disorders.", 18, "Surgery Center");
        doctorService.updateDoctor(doctor3);

        // Create Doctor 4 - Dermatologist
        User doctor4User = new User();
        doctor4User.setFirstName("Emily");
        doctor4User.setLastName("Davis");
        doctor4User.setEmail("dr.davis@medbook.com");
        doctor4User.setPassword("password123");
        doctor4User.setPhoneNumber("+27 72 456 7890");
        doctor4User.setRole(User.Role.DOCTOR);
        userService.registerUser(doctor4User);

        Doctor doctor4 = new Doctor(doctor4User, "Dermatology", "MD456789", 
                                 "Dermatologist specializing in skin conditions, cosmetic procedures, and skin cancer treatment.", 10, "Main Hospital");
        doctorService.updateDoctor(doctor4);

        // Create Doctor 5 - Pediatrician
        User doctor5User = new User();
        doctor5User.setFirstName("David");
        doctor5User.setLastName("Wilson");
        doctor5User.setEmail("dr.wilson@medbook.com");
        doctor5User.setPassword("password123");
        doctor5User.setPhoneNumber("+27 72 567 8901");
        doctor5User.setRole(User.Role.DOCTOR);
        userService.registerUser(doctor5User);

        Doctor doctor5 = new Doctor(doctor5User, "Pediatrics", "MD567890", 
                                 "Pediatrician with 14 years of experience in child healthcare, vaccinations, and developmental issues.", 14, "Pediatrics Wing");
        doctorService.updateDoctor(doctor5);

        // Create Doctor 6 - Ophthalmologist
        User doctor6User = new User();
        doctor6User.setFirstName("Lisa");
        doctor6User.setLastName("Anderson");
        doctor6User.setEmail("dr.anderson@medbook.com");
        doctor6User.setPassword("password123");
        doctor6User.setPhoneNumber("+27 72 678 9012");
        doctor6User.setRole(User.Role.DOCTOR);
        userService.registerUser(doctor6User);

        Doctor doctor6 = new Doctor(doctor6User, "Ophthalmology", "MD678901", 
                                 "Ophthalmologist specializing in eye surgery, cataract treatment, and vision correction procedures.", 16, "Main Hospital");
        doctorService.updateDoctor(doctor6);

        // Create Doctor 7 - Gastroenterologist
        User doctor7User = new User();
        doctor7User.setFirstName("Robert");
        doctor7User.setLastName("Taylor");
        doctor7User.setEmail("dr.taylor@medbook.com");
        doctor7User.setPassword("password123");
        doctor7User.setPhoneNumber("+27 72 789 0123");
        doctor7User.setRole(User.Role.DOCTOR);
        userService.registerUser(doctor7User);

        Doctor doctor7 = new Doctor(doctor7User, "Gastroenterology", "MD789012", 
                                 "Gastroenterologist with expertise in digestive disorders, endoscopy, and liver diseases.", 13, "Main Hospital");
        doctorService.updateDoctor(doctor7);

        // Create Doctor 8 - Psychiatrist
        User doctor8User = new User();
        doctor8User.setFirstName("Jennifer");
        doctor8User.setLastName("Martinez");
        doctor8User.setEmail("dr.martinez@medbook.com");
        doctor8User.setPassword("password123");
        doctor8User.setPhoneNumber("+27 72 890 1234");
        doctor8User.setRole(User.Role.DOCTOR);
        userService.registerUser(doctor8User);

        Doctor doctor8 = new Doctor(doctor8User, "Psychiatry", "MD890123", 
                                 "Psychiatrist specializing in mental health disorders, anxiety, depression, and therapy.", 11, "Main Hospital");
        doctorService.updateDoctor(doctor8);

        // Create Doctor 9 - Urologist
        User doctor9User = new User();
        doctor9User.setFirstName("Christopher");
        doctor9User.setLastName("Garcia");
        doctor9User.setEmail("dr.garcia@medbook.com");
        doctor9User.setPassword("password123");
        doctor9User.setPhoneNumber("+27 72 901 2345");
        doctor9User.setRole(User.Role.DOCTOR);
        userService.registerUser(doctor9User);

        Doctor doctor9 = new Doctor(doctor9User, "Urology", "MD901234", 
                                 "Urologist with expertise in kidney stones, prostate conditions, and urological surgeries.", 17, "Surgery Center");
        doctorService.updateDoctor(doctor9);

        // Create Doctor 10 - Gynecologist
        User doctor10User = new User();
        doctor10User.setFirstName("Amanda");
        doctor10User.setLastName("Lee");
        doctor10User.setEmail("dr.lee@medbook.com");
        doctor10User.setPassword("password123");
        doctor10User.setPhoneNumber("+27 72 012 3456");
        doctor10User.setRole(User.Role.DOCTOR);
        userService.registerUser(doctor10User);

        Doctor doctor10 = new Doctor(doctor10User, "Gynecology", "MD012345", 
                                 "Gynecologist specializing in women's health, pregnancy care, and reproductive health.", 9, "Main Hospital");
        doctorService.updateDoctor(doctor10);
    }
}