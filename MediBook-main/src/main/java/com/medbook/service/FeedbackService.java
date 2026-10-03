package com.medbook.service;

import com.medbook.entity.Feedback;
import com.medbook.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public Feedback saveFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }

    public Feedback findById(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
    }

    public void markAsRead(Long id) {
        Feedback feedback = findById(id);
        feedback.setReadFlag(true);
        feedbackRepository.save(feedback);
    }

    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }

    public long getUnreadCount() {
        return feedbackRepository.countUnreadFeedback();
    }

    public List<Feedback> getUnreadFeedback() {
        return feedbackRepository.findByReadFlagFalseOrderByCreatedAtDesc();
    }
}
