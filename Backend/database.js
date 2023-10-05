const mysql = require('mysql2');
var connect = mysql.createConnection({
    host: '127.0.0.1',
    database:'licenta',
    user: 'gabi.balanescu',
    password: '123qweASD'
});

connect.connect(err => {
    if (err) {throw err}
    console.log('Mysql is connect') 
});

module.exports= connect;