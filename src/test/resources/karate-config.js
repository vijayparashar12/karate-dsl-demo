function() {
    var config = {
    getRequest : function(title, name) {
                   var randLetter = String.fromCharCode(65 + Math.floor(Math.random() * 26));
                   var uniqueId = randLetter + Date.now();
                   var person = {};
                   person['id'] = uniqueId;
                   person['title'] = title;
                   person['name'] = name;
                   return person
                 }
    };
    return config;



}
