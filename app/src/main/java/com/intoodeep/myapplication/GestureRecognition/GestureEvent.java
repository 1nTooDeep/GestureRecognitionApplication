package com.intoodeep.myapplication.GestureRecognition;

public class GestureEvent {
    public static enum Gestures {
        SLIDING_TWO_FINGERS_UP(0),
        SLIDING_TWO_FINGERS_LEFT(1),
        SLIDING_TWO_FINGERS_RIGHT(2),
        SLIDING_TWO_FINGERS_DOWN(3),
        ZOOMING_IN_WITH_TWO_FINGERS(4),
        ZOOMING_OUT_WITH_TWO_FINGERS(5),
        SWIPING_RIGHT(6),
        SWIPING_UP(7),
        SWIPING_DOWN(8),
        DOING_OTHER_THINGS(9),
        NO_GESTURE(10);

        private final int value;

        Gestures(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        // 可以添加一个静态方法，通过整数值查找对应的枚举项
        public static Gestures fromInt(int value) {
            for (Gestures gesture : values()) {
                if (gesture.getValue() == value) {
                    return gesture;
                }
            }
            throw new IllegalArgumentException("Invalid gesture value");
        }
    }
}