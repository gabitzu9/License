const express = require("express")
const bodyParser = require('body-parser')
const bcrypt = require("bcrypt")
const app = express()

const jsonParser = bodyParser.json()
const db = require('../APP_BE/database')
const e = require("express")
// Funcție pentru hash-uit parolă
async function hashPassword(password) {
  const saltRounds = 10;
  return bcrypt.hash(password, saltRounds);
}


app.post('/signup',jsonParser, (req,res)=>{

 // hash = bcrypt.hash(req.body.password,10)
  var find_query = 'Select * from users where email = \'' + req.body.email + '\''
  db.query(find_query, function (err,data) {
    if (err){   
      console.log("Err-signup-search _users_") 
      res.status(200).send("Database Error" ); 
      throw err
    }
    else{
      if(data.length >= 1){
        console.log("Current email already exists in database") 
        res.status(200).send("Current email already exists in database");
      }
      else{
        //console.log(data)
        var name = req.body.name
        var surname = req.body.surname
        var email = req.body.email 
        var nr_tel = req.body.phone
        var password = req.body.pass//hash 
        var sql = 'INSERT INTO users (nume,prenume,email,nr_tel,parola) VALUES (\'' + name +'\',\''+ surname +'\',\''+ email +'\',\''+ nr_tel+'\',\''+ password +'\');'
        var id_traseu 
        db.query(sql, function (err, data) {
            console.log(data)
            if (err)  throw err
            console.log('Database updated successfully')
            res.status(200).send("Database updated successfully");
          })
        }
      }
  })
});

app.post('/login',jsonParser, (req,res) => {
  
  var find_query = 'Select * from users where email = \'' + req.body.email + '\''
  db.query(find_query, function (err,data) {
    if (err){
      console.log("Err-login-search _users_") 
      res.status(200).send("Database Error");
      throw err
    }
    else{
      if(data.length < 1){
        console.log("Autentification failed");
        res.status(200).send("Autentification failed");
      }
      else{
          var email = req.body.email
          var password = req.body.pass
          var databasePassword = data[0].parola

          if (password != databasePassword){
              console.log("Invalid password")
              res.status(200).send("Invalid password");
          }
          else{
              console.log("logged in successfully")
              res.status(200).send("Logged in successfully");
          }
      }
    }
  })
});

app.get('/get_routes/:email', (req, res) => {

  var email = req.params.email;
  var find_query = 'Select * from users where email = \'' + email + '\''
    db.query(find_query, function (err,data) {
      if (err){   
            console.log("Err-rec-search _users_") 
            res.status(200).send("Database Error" ); 
            throw err
      }
      else{
            var id_user=data[0].id_user
            var routeQuery = 'SELECT * FROM rec_traseu WHERE id_user = \''+ id_user + '\'';
            db.query(routeQuery, function(err, routeData) {
                if (err) {
                      console.error("Error getting route data:", err);
                      res.status(500).json({ error: "Failed to retrieve route data" });
                      return;
                }else{
                    var allData = [];

                    routeData.forEach(route => {

                        var pointsQuery = 'SELECT * FROM ruta WHERE id_traseu = ?';
                        db.query(pointsQuery, [route.id_traseu], function(err, pointsData) {
                            if (err) {
                                console.error("Error getting points data:", err);
                                res.status(500).json({ error: "Failed to retrieve points data" });
                                return;
                            }else{

                                var routeWithPoints = {
                                    route: route,
                                    points: pointsData
                                };

                                allData.push(routeWithPoints);

                                if (allData.length === routeData.length) {
                                    res.status(200).json(allData);
                                }
                            }
                        });
                    });
                }
            });
          }
    });
});

app.post('/rec',jsonParser, (req,res)=>{

    console.log(req);
    var find_query = 'Select * from users where email = \'' + req.body.email + '\''
    db.query(find_query, function (err,data) {
      if (err){   
            console.log("Err-rec-search _users_") 
            res.status(200).send("Database Error" ); 
            throw err
      }
      else{
            var id_user=data[0].id_user
            var nume_traseu=req.body.nume_traseu
            var data_zilei = new Date()
            var formeazaData = data_zilei.toISOString().split('T')[0]
            var start_loc = req.body.start_loc
            var finish_loc = req.body.finish_loc
            var sql = 'INSERT INTO rec_traseu (id_user,nume_traseu,data_zilei,start_loc,finish_loc) VALUES (\'' + id_user +'\',\''+ nume_traseu +'\',\''+ formeazaData +'\',\''+ start_loc+'\',\''+ finish_loc +'\');'
            
            
            db.query(sql, function (err, data) {
                if (err)  throw err
                else{
                    console.log(data)
                    console.log('Database(rec_tarseu) updated successfully')
                    var id_traseu = data.insertId
                    var size =req.body.coordinatesDTOList.length
                    for(i=0;i<size;i++)
                    {
                        var nr_ord_poz_ruta = i+1
                        var lat = req.body.coordinatesDTOList[i].lat
                        var lng = req.body.coordinatesDTOList[i].lng
                        var sql = 'INSERT INTO ruta (id_traseu,nr_ord_poz_ruta,lat,lng) VALUES (\'' + id_traseu +'\',\''+ nr_ord_poz_ruta +'\',\''+ lat +'\',\''+ lng+'\');'
                        db.query(sql, function (err, data) {
                            console.log(data)
                            if (err)  throw err
                            console.log('Database(ruta) updated successfully')
                            
                        });
                        if (i === size) {
                          res.status(200).send("Database updated successfully"); 
                        }
                    }
                }
            });
        }
    })
});

app.post('/delete_rec',jsonParser, (req,res)=>{

  var find_query = 'Select * from users where email = \'' + req.body.email + '\''
  db.query(find_query, function (err,data) {
      if (err){   
            res.status(200).send("Database Error" ); 
            console.log('Database Error - search from _users_ ')
            throw err
      }else{
            var cauta_nume_traseu=req.body.cauta_nume_traseu
            var id_user=data[0].id_user
            var sql = 'SELECT * FROM rec_traseu WHERE nume_traseu = \''+ cauta_nume_traseu + '\' and id_user = \'' + id_user +'\';'
            
            db.query(sql, function (err, data) {
                  console.log(data)
                  if (err) { 
                        //res.status(200).send("Database Error" ); 
                        console.log('Database Error - search from _rec_traseu_ ')
                        throw err
                  }else{
                        var id_traseu = data[0].id_traseu
                        var sql = 'DELETE FROM ruta WHERE id_traseu = \''+ id_traseu + '\' ;'
                        db.query(sql, function(err, data){
                              if (err) { 
                                      //res.status(200).send("Database Error" ); 
                                      console.log('Database Error - delete from _ruta_ ')
                                      throw err

                              }else   console.log('Database updated - delete from _ruta_ ')
                        });
        
                        var sqll = 'DELETE FROM rec_traseu WHERE nume_traseu = \''+ cauta_nume_traseu +'\'and id_user = \'' + id_user +'\';'
                        db.query(sqll, function (err, data) {
                                if (err) {
                                        //res.status(200).send("Database Error" ); 
                                        console.log('Database Error - delete from _nume_traseu_ ')
                                        throw err

                                }else   console.log('Database updated - delete from _nume_tabela_ ')
                        });
                  }
            });
      }
  });
  res.status(200).send("Database updated - DELETE");
    
});

app.listen(3000, function () {
  console.log("server started successfully");
});
