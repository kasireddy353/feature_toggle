const http = require('http'), fs = require('fs')
var redis = require('redis'), client = redis.createClient();
var express = require('express'), app = express();
var bodyParser = require('body-parser'), urlencodedParser = bodyParser.urlencoded({extended: true});


var server = app.listen(3000, function(){
    var host = server.address().address
    var port = server.address().port
    console.log("App listening at %s:%s Port", host, port)
});

app.get('/form', function(req, res){
    fs.readFile('./featuretoggle.html', function(err, html){

        if(err){
            throw err;
        }

        res.writeHeader(200, {"Content-Type":"text/html"});
        res.write(html);
        res.end();

    });


});


app.post('/thank', urlencodedParser, function(req, res){

    console.log('came inside');
    var toggleValue = String(req.body.radio_toggle);
    var keyName = String(req.body.keyname);
    console.log(toggleValue);

    client.set(keyName, keyName + ':' + toggleValue, redis.print);


    res.writeHeader(200,{"Content-Type":"text/html"});
    var reply = '<h2>Successfully toggled the feature</h2>';
    res.write(reply);
    res.end();

});
