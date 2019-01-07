/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.device.collectors;

import android.os.Bundle;

import androidx.annotation.VisibleForTesting;

import com.android.helpers.ICollectorHelper;

import org.junit.runner.Description;
import org.junit.runner.Result;

import java.util.Map;

/**
 * A {@link BaseCollectionListener} that captures metrics collected during the testing.
 *
 * Metrics can be collected at the test run level or per test method using per_run option.
 *
 * If there are any failure in the metric collection, tests will still proceed to run and
 * not posting the metrics at the end of the test.
 *
 * Do NOT throw exception anywhere in this class. We don't want to halt the test when metrics
 * collection fails.
 */
public class BaseCollectionListener<T> extends BaseMetricListener {

    private ICollectorHelper mHelper;
    // Collect per run if it is set to true otherwise collect per test.
    public static final String COLLECT_PER_RUN = "per_run";
    protected boolean mIsCollectPerRun;

    public BaseCollectionListener() {
        super();
    }

    @VisibleForTesting
    public BaseCollectionListener(Bundle args, ICollectorHelper helper) {
        super(args);
        mHelper = helper;
    }

    @Override
    public void onTestRunStart(DataRecord runData, Description description) {
        Bundle args = getArgsBundle();
        mIsCollectPerRun = "true".equals(args.getString(COLLECT_PER_RUN));

        if (mIsCollectPerRun) {
            mHelper.startCollecting();
        }

    }

    @Override
    public void onTestStart(DataRecord testData, Description description) {
        if (!mIsCollectPerRun) {
            mHelper.startCollecting();
        }
    }

    @Override
    public void onTestEnd(DataRecord testData, Description description) {
        if (!mIsCollectPerRun) {
            Map<String, T> metrics = mHelper.getMetrics();
            for (Map.Entry<String, T> entry : metrics.entrySet()) {
                testData.addStringMetric(entry.getKey(), entry.getValue().toString());
            }
            mHelper.stopCollecting();
        }
    }

    @Override
    public void onTestRunEnd(DataRecord runData, Result result) {
        if (mIsCollectPerRun) {
            Map<String, T> metrics = mHelper.getMetrics();
            for (Map.Entry<String, T> entry : metrics.entrySet()) {
                runData.addStringMetric(entry.getKey(), entry.getValue().toString());
            }
            mHelper.stopCollecting();
        }
    }

    protected void createHelperInstance(ICollectorHelper helper) {
        mHelper = helper;
    }

}
