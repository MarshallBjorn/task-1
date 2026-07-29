package com.navrotskyi.trippyapi.validation;

import java.time.temporal.Temporal;

public interface DateRangeProvider {
    Temporal getRangeStart();
    Temporal getRangeEnd();
}