package org.balance.extractor.processes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.balance.extractor.gui.ui.components.Progress;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author Nicholas Curl
 */
public abstract class Extractor {

    /**
     * The instance of the logger
     */
    private static final Logger               logger    = LogManager.getLogger(Extractor.class);
    private static final DateTimeFormatter    formatter = DateTimeFormatter.ofPattern("MM/dd/yy");
    private final        Map<String, Task<?>> tasks;

    public Extractor(Map<String, Task<?>> tasks) {
        this.tasks = tasks;
    }

    public abstract void extract();

    public Map<String, Task<?>> getTasks() {
        return tasks;
    }

    protected void addTask(Task<?> task) {
        tasks.put(formatter.format(task.period), task);
    }

    protected void removeTask(Task<?> task) {
        tasks.remove(formatter.format(task.period));
    }

    public abstract static class Task<K> extends SwingWorker<K, Void> {

        /**
         * The instance of the logger
         */
        private static final Logger logger = LogManager.getLogger(Task.class);

        private final JProgressBar overallProgress;
        private final Progress     progressContainer;
        private final LocalDate    period;

        public Task(Progress progress, JProgressBar overallProgress, String period
        ) {
            this.progressContainer = progress;
            this.overallProgress = overallProgress;
            String[] split = period.split("C");
            LocalDate periodTemp;
            try {
                periodTemp = LocalDate.from(formatter.parse(split[1]));
            } catch (ArrayIndexOutOfBoundsException e){
                periodTemp = LocalDate.from(formatter.parse(split[0]));
            }
            this.period = periodTemp;
        }

        public JProgressBar getOverallProgress() {
            return overallProgress;
        }

        public Progress getProgressContainer() {
            return progressContainer;
        }

        public LocalDate getPeriod() {
            return period;
        }
    }
}
