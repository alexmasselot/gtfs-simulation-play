const http = require('http');

var countLines = 0;
http.get('http://localhost:9000/simulator/positions-bounded?minLat=46&maxLat=48&minLng=5&maxLng=10', (res) => {

    res.on('data',  (chunk) =>{
    const str = chunk.toString();
    var i = (str.match(/data:/g) || []).length;
    countLines+=i;
});
});

var stepInterval = 1;
var t0 =new Date().getTime();

setInterval(function(){
    const t1 = new Date().getTime();
    console.log(((t1-t0)/1000)+'\t'+countLines);
    countLines=0;
}, stepInterval*1000);