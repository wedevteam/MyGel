package com.wedev.mygel.functions;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;

public class MessaggiJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Intent service = new Intent(getApplicationContext(), LocalService.class);
        getApplicationContext().startService(service);
        Util.scheduleJob(getApplicationContext()); // reschedule the job
        return true;
    }
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return true;
    }
}

