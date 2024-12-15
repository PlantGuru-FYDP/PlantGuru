const TIME_SETTINGS = {
    GRANULARITIES: {
        RAW: 0,
        MINUTE_5: 1,
        MINUTE_15: 5,
        MINUTE_30: 10,
        HOUR: 15,
        HOUR_12: 180,
        DAY: 360,
        WEEK: 2520,
        MONTH: 10800
    },
    TIME_RANGES: {
        HOUR: 1,
        DAY: 24,
        WEEK: 24 * 7,
        MONTH: 24 * 30
    },
    GRANULARITY_THRESHOLDS: {
        0: 1,
        1: 3,
        5: 12,
        10: 24,
        15: 84,
        180: 168,
        360: 360,
        2520: 1080,
        10800: 4380
    },
    MAX_TIME_RANGE_DAYS: 90,
    DEFAULT_TIME_RANGE: 'DAY'
};

const PROJECTION_SETTINGS = {
    TIME_RANGES: {
        HOUR: {
            hours: 1,
            granularity: 2,  // 2 minutes
            pastHours: 1
        },
        TWELVE_HOURS: {
            hours: 12,
            granularity: 15, // 15 minutes
            pastHours: 12
        },
        DAY: {
            hours: 24,
            granularity: 15, // 15 minutes
            pastHours: 24
        },
        WEEK: {
            hours: 24 * 7,
            granularity: 360, // 360 minutes (6 hours)
            pastHours: 24 * 7
        },
        MONTH: {
            hours: 24 * 30,
            granularity: 720, // 720 minutes (12 hours)
            pastHours: 24 * 30
        }
    }
};

module.exports = { TIME_SETTINGS, PROJECTION_SETTINGS }; 