package com.medbook.controller;

import com.medbook.entity.Doctor;
import com.medbook.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/")
    public String home(Model model) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("featuredDoctors", doctors.stream().limit(6).toList());
        return "home";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }


    @GetMapping("/partners")
    public String partners() {
        return "partners";
    }

    @GetMapping("/services")
    public String services() {
        return "services";
    }

    @GetMapping("/search")
    public String searchDoctors(@RequestParam(required = false) String query, Model model) {
        List<Doctor> doctors;
        
        if (query != null && !query.trim().isEmpty()) {
            // Search doctors by name, specialization, or license number (case-insensitive)
            doctors = doctorService.searchDoctors(query.trim());
            model.addAttribute("searchQuery", query.trim());
            model.addAttribute("searchResults", doctors);
        } else {
            // If no query, show all doctors
            doctors = doctorService.getAllDoctors();
            model.addAttribute("searchResults", doctors);
        }
        
        model.addAttribute("featuredDoctors", doctors.stream().limit(6).toList());
        return "search-results";
    }
}