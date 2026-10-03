package com.medbook.controller;

import com.medbook.entity.Feedback;
import com.medbook.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @PostMapping("/contact")
    public String submitFeedback(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam String subject,
                                @RequestParam String message,
                                RedirectAttributes redirectAttributes) {
        try {
            Feedback feedback = new Feedback(firstName, lastName, email, subject, message);
            feedbackService.saveFeedback(feedback);
            redirectAttributes.addFlashAttribute("success", "Thank you for your feedback! We'll get back to you soon.");
            return "redirect:/contact?success=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to send message: " + e.getMessage());
            return "redirect:/contact";
        }
    }
}
