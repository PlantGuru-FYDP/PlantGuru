const TIME_SETTINGS = {
    GRANULARITIES: {
        RAW: 0,
        MINUTE_5: 5,
        MINUTE_15: 15,
        MINUTE_30: 30,
        HOUR: 60,
        HOUR_12: 720,
        DAY: 1440,
        WEEK: 10080,
        MONTH: 43200
    },
    TIME_RANGES: {
        HOUR: 1,
        DAY: 24,
        WEEK: 24 * 7,
        MONTH: 24 * 30
    },
    GRANULARITY_THRESHOLDS: {
        0: 2,        // Raw data up to 2 hours
        5: 6,        // 5min granularity up to 6 hours
        15: 24,      // 15min granularity up to 24 hours
        30: 48,      // 30min granularity up to 48 hours
        60: 168,     // 1hour granularity up to 1 week
        720: 336,    // 12hour granularity up to 2 weeks
        1440: 720,   // 1day granularity up to 1 month
        10080: 2160, // 1week granularity up to 3 months
        43200: 8760  // 1month granularity up to 1 year
    },
    MAX_TIME_RANGE_DAYS: 90,
    DEFAULT_TIME_RANGE: 'DAY'
};

const PROJECTION_SETTINGS = {
    TIME_RANGES: {
        HOUR: {
            hours: 1,
            granularity: 5,  // 5 minutes
            pastHours: 1
        },
        TWELVE_HOURS: {
            hours: 12,
            granularity: 30, // 30 minutes
            pastHours: 12
        },
        DAY: {
            hours: 24,
            granularity: 30, // 30 minutes
            pastHours: 24
        },
        WEEK: {
            hours: 24 * 7,
            granularity: 720, // 12 hours
            pastHours: 24 * 7
        },
        MONTH: {
            hours: 24 * 30,
            granularity: 1440, // 1 day
            pastHours: 24 * 30
        }
    }
};

module.exports = { TIME_SETTINGS, PROJECTION_SETTINGS }; 