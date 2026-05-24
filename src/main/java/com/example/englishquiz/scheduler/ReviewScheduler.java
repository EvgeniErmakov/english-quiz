package com.example.englishquiz.scheduler;

import com.example.englishquiz.scheduler.dto.ReviewScheduleInput;
import com.example.englishquiz.scheduler.dto.ReviewScheduleResult;

public interface ReviewScheduler {

    ReviewScheduleResult schedule(ReviewScheduleInput input);
}
