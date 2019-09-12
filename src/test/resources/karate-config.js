function() {
    var NakadiEventListener = Java.type('parashar.vijay.karate.demo.nakadi.NakadiEventListener');
    var nakadi = NakadiEventListener.instance();

    var config = {
        httpBin: 'https://httpbin.org',
        token_file: '/Users/vparashar/Documents/workspace/credentials/token.txt',
        workflow_network: 'https://workflow-repository-dev.logistics.zalan.do',
        process_instance_generator: 'https://process-instance-generator-dev.logistics-test.zalan.do',
        movementBaseURL:'https://movement-service-dev.logistics-test.zalan.do'

    };
    return config;
}
