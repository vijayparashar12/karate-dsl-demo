function fn(eventType, whereClause) {
    var NakadiEventListener = Java.type('parashar.vijay.karate.demo.nakadi.NakadiEventListener');
    var nakadi = NakadiEventListener.instance();
    return nakadi.event(eventType,whereClause)
}