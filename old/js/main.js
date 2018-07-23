var cnv = document.getElementById("canvas");
var ctx = cnv.getContext("2d");
ctx.mozImageSmoothingEnabled = false;
ctx.webkitImageSmoothingEnabled = false;
ctx.msImageSmoothingEnabled = false;
ctx.imageSmoothingEnabled = false;

var gameEngine;

//автотайл
var img = new Image();   // Создает новый элемент изображения
img.src = './img/autotile.png'; // Устанавливает путь

var imgGround = new Image();   // Создает новый элемент изображения
imgGround.src = './img/ground.png'; // Устанавливает путь

var imgBackground = new Image();   // Создает новый элемент изображения
imgBackground.src = './img/background.jpg'; // Устанавливает путь

var imgWall = new Image();
imgWall.src = './img/wall.jpg';
var imgWallRoof = new Image();
imgWallRoof.src = './img/wallRoof.jpg';

var doorImg = new Image();
doorImg.src = './img/door.png';

var tileWidht = 32;

//из индекса в координаты тайла
var idTile = {"1": [0,0], "2": [0,32], "3": [0,64], "4": [0,96], "5": [0,128], "6": [0,160], "7": [0,192], "8": [0,224], 
"9": [32,0], "10": [32,32], "11": [32,64], "12": [32,96], "13": [32,128], "14": [32,160], "15": [32,192], "16": [32,224], 
"17": [64,0], "18": [64,32], "19": [64,64], "20": [64,96], "21": [64,128], "22": [64,160], "23": [64,192], "24": [64,224], 
"25": [96,0], "26": [96,32], "27": [96,64], "28": [96,96], "29": [96,128], "30": [96,160], "31": [96,192], "32": [96,224], 
"33": [128,0], "34": [128,32], "35": [128,64], "36": [128,96], "37": [128,128], "38": [128,160], "39": [128,192], "40": [128,224], 
"41": [160,0], "42": [160,32], "43": [160,64], "44": [160,96], "45": [160,128], "46": [160,160], "47": [160,192], "48": [160,224],
"0": [160,192], "undefined": [160,192]};

var getTile = { "0":47, "undefined":47, /* СОЧЛЕНЕНИЯ: */
"2":45, "3":45, "6":45, "7":45, "8":46, "9":46, "10":40, "11":39, "14":40, "15":39, "16":44, "18":42, "19":42, 
"20":44, "22":41, "23":41, "24":34, "25":34, "26":32, "27":31, "28":34, "29":34, /*совпало: */ "30":30, "31":29,
"40":46, "41":46, "42":40, "43":39, "46":40, "47":39, "56":34, "57":34, "58":32, "59":31, "60":34, "61":34, "62":30, "63":29, "64":43, "66":33, "67":33, 
 "70":33, "71":33, "72":38, "73":38, "74":28, "75":26, "78":28, "79":26, "80":36, "82":20, "83":20, "84":36, "86":19, "87":19, "88":24, "89":24, 
"90":16, "91":15, "92":24, "93":24, "94":14, "95":13, "96":43, "98":33, "99":33, 
"102":33, "103":33, "104":37, "105":37, "106":27, "107":25, "110":27, "111":25, "112":36, "115":20, "114":20, "116":36, "118":19, "119":19,
"120":22, "121":22, "122":8, "123":7, "124":22, "125":22, "126":6, "127":5, "144":44, "146":42, "147":42, "148":44, "150":41, 
"151":41, "152":34, "153":34, "154":32, "155":31, "156":34, "157":34, "158":30,
"159":29, "184":34, "185":34, "186":32, "187":31, "188":34, "189":34, "190":30, "191":29, "192":43, "194":33, "195":33, "198":33, "199":33, "200":38, 
"202":28, "206":28, "207":26, "208":35, "210":18, "211":18, "212":35, "214":17, "215":17, "216":23, "217":23, "218":12, "219":11,
"220":23, "203":26, "201":38, "221":23, "222":10 , "223":9, "224":43, "226":33, "227":33, 
"230":33, "231":33, "232":37, "233":37, "234":27, "235":25, "238":27, "239":25, "240":35, "242":18, 
"243":18, "244":35, "246":17, "247":17, "248":21, "249":21, "250":4, "251":3, "252":21, "253":21, "254":2, "255":1, 

/* БЕЗ СОЧЛЕНЕНИЙ: */
"12":46, "13":46, "17":44, "21":44, "34": 45, "35":45, "38":45, "39":45, "44":46, "45":46, "48":44, "49":44, 
"50":42, "51":42, "52":44, "53":44, "54":41, "55":41, "65":43, "68":43, "69":43, "76":38, "77":38, "81":36, "85":36, "97":43, 
"100":43, "101":43, "108":37, "109":37, "113":36, "117":36, "130": 45, "131":45, "134":45, "135":45, "136":46, "137":46, "138":40, "139":39, 
"140":46, "141":46, "142":40, "143":39, "145":44, "149":44, "162":45, "163":45, "166":45, "167":45, "168":46, "169":46, "170":40, "171":39, "172":46, "173":46, 
"174":40, "175":39, "176":44, "177":44, "178":42, "179":42, "180":44, "181":44, "182":41, "183":41, "193":43, "196":43, "197":43, "204":38, "205":38,
"209":35, "213":35, "225":43, "228":43, "229":43, "236":37, "237":37, "241":35, "245":35};












////////////////////ОБРАБОТКА КЛАВЫ////////////////////
var keys = {
	"W": 87,
	"A": 65,
	"S": 83,
	"D": 68,
	"G": 71,

	"Q": 81,
	"E": 69,
	"F":70,


	"Z":90, //для автотайлинга мышью
	"X":88, //для создания деревьев мышью
	"C":67, //для отмены состоянии мыши
	//"V":86, //для выделения
	"B":66, //создание зон
	"N":78, //удаление зон
	"M":77, //создание стен
	"O":79, //открытие/закрытие двери

	"Up": 38,
	"Left": 37,
	"Right": 39,
	"Down":40
};

var keyDown = {};

var setKey = function (keyCode){
	keyDown[keyCode] = true;
};

var clearKey = function (keyCode){
	keyDown[keyCode] = false;
};

var isKeyDown = function (keyName){
	return keyDown[ keys[keyName] ];
};

//проверяет, нажата ли хоть одна кнопка или при keyName - нажаты ли они
var isKeysDown = function (keyName){

	if (keyName === undefined){
		for (var key in keyDown){
			if (keyDown[key]) return true;
		}
	} else {

	for (var key1 in keyName){
			if (keyDown[ keys[ keyName[ key1] ] ]) return true;
		}
	}

	return false;
};


window.onload = function (){


	window.onkeydown = function (e) {
		setKey(e.keyCode);
		//узнать код нажатой клавиши
		//console.log (e.keyCode);
	};


	window.onkeyup = function (e) {
		clearKey(e.keyCode);
	};

};














////////////////////КАМЕРА////////////////////
var camera = {
	x : 0,
	y : 0,
	scale : 1,
	tabu : 200, //ограничение на перемещение

	shiftX:0,
	shiftY:0,

	center: function(){
		var xc = cnv.width/2 - Grid.x * tileWidht * this.scale/2;
		this.x = -xc/ this.scale;

		var yc = cnv.height/2 - Grid.y * tileWidht * this.scale/2;
		this.y = -yc/ this.scale;

		//!!!!!!!!!!!!!!!!!!ВОТ ЗДЕСЬ КОСЯК!!!!!!!!!!!!!!!!!!
		this.shiftX = 0;
		this.shiftY = 0;
	},

	shift: function(){


		var xc = cnv.width/2 - Grid.x * tileWidht * this.scale/2;
		this.x = -xc/ this.scale + this.shiftX;
		var yc = cnv.height/2 - Grid.y * tileWidht * this.scale/2;
		this.y = -yc/ this.scale + this.shiftY;

	},


	zoom : function(size){
		this.scale += size*0.01;
		//не очень элегантное решение ограничения на масштабирование
		if (this.scale >= 2 || this.scale <= 0.5) this.scale -= size*0.01;

		this.shift();

		ctx.setTransform(this.scale, 0, 0, this.scale, 0, 0);
	},

	translate : function(xx , yy){
		this.x += xx;
		this.y += yy;
		this.shiftX += xx;
		this.shiftY += yy;
	}
};












//мышка
var Mouse = {
	x:0,
	y:0,
	_condition: 0,

	initialization: function(){
		_condition = "none";
	},

	update: function(){
		if (isKeyDown("Z")){
			_condition = "AutoTile";
			Create1.delete();
			Selected.delSelected();
		}
		if (isKeyDown("X")){
			_condition = "CreateTree";
			Create1.new("./img/Grassland.png", 0, 160, 128, 160);
		}
		if (isKeyDown("C")){
			_condition = "none";
			Create1.delete();
			Selected.delSelected();
		}
		if (isKeyDown("B")){
			_condition = "zone";
			Create1.delete();
			Selected.delSelected();
		}
		if (isKeyDown("N")){
			_condition = "DelZone";
			Create1.delete();
			Selected.delSelected();
		}
		if (isKeyDown("M")){
			_condition = "createWall";
			Create1.delete();
			Selected.delSelected();
		}
		if (isKeyDown("O")){
			Door.trigger();
		}
	},

	GetCondition: function(){
		return (_condition != "none") ? _condition : false;
	},

	SetCondition: function(asf){
		_condition = asf;
	}

};

Mouse.initialization();

window.onmousemove =  function(e) {
	var mX = (e.pageX - e.target.offsetLeft)/camera.scale  + camera.x;
	var mY = (e.pageY - e.target.offsetTop)/camera.scale + camera.y;

	var pick = collides (mX, mY, 0, 0, Grid.x*tileWidht, Grid.y*tileWidht);
	if (pick) {
		Mouse.x = pick[0];
		Mouse.y = pick[1];
		//console.log(pick);
	}
};

cnv.addEventListener('click', function(e) {
	var mX = (e.pageX - e.target.offsetLeft)/camera.scale  + camera.x;
	var mY = (e.pageY - e.target.offsetTop)/camera.scale + camera.y;
	var pick = collides (mX, mY, 0, 0, Grid.x*tileWidht, Grid.y*tileWidht);
	//if (!point1) var point1 = false; //первая точка создания зоны

 	if (!this.point1) this.point1 = false;

	if (pick && Mouse.GetCondition() == "AutoTile") {

		Grid.setID ( Grid.getXY(pick[0],pick[1])[0], Grid.getXY(pick[0],pick[1])[1], 1);
		this.point1 = false;
		//console.log(pick);
		//this.break; //иначе пойдет считать следующие ифы
	}

	if (pick && Mouse.GetCondition() == "CreateTree") {
		Create1.getXY(); //на всякий случай обновим x и y по сетке
		Layer.create(Create1.x, Create1.y, Create1.img.src, Create1.imgX, Create1.imgY, Create1.width, Create1.height, "Tree");

		Create1.delete();
		Mouse.SetCondition("none");
		this.point1 = false;

		//this.break; //иначе пойдет считать следующие ифы
	}


	if (pick && !Mouse.GetCondition()) {

		//console.log("fdfdg");

		for (var i = 0; i < Layer.length(); i++) {

			var ii = Layer.getObj(i);

			if (collides(mX, mY, ii.bbx, ii.bby, ii.bbx+ii.bbw, ii.bby+ii.bbh) && ii.Entity){

//////////////////////////////ЕСЛИ ОБЪЕКТ ИМЕЕТ ENTITY ТО БУДЕТ СЕЛЕКНУТ/////////////////////////

				//console.log(ii.Entity);

				Selected.setSelected(ii.Entity);
				Mouse.SetCondition("Selected");
				this.point1 = false;
			}

		}


		this.break(); //иначе пойдет считать следующие ифы
	}


	if (pick && Mouse.GetCondition() == "Selected") {

		var pp1 = new PointClass(Grid.getXY(pick[0],pick[1])[0], Grid.getXY(pick[0],pick[1])[1]);
		var pp = new Path(pp1, Selected.getSelected().Point, Selected.getSelected());
		this.point1 = false;

		Orders.newPoint(pp1);

		//ВАЖНО!!!!!!!!!!!!! //pp.newPoint(Selected.getSelected().Point); //КРИВЫЕ КООРДИНАТЫ




		pp.findPath();



		Paths.newPath(pp);
	}

	if (pick && Mouse.GetCondition() == "zone" && !this.point1) {

		//console.log(this.point1);
/////////////////////////////////////////ВНИМАНИЕ!! НЕ ПОНИМАЮ, ПОЧЕМУ -2, А НЕ -1////////////////////////////////////
		this.point1 = new PointClass(Grid.getXY(pick[0],pick[1])[0]-2, Grid.getXY(pick[0],pick[1])[1]-2);

		//console.log(this.point1);
		this.break(); //иначе пойдет считать следующие ифы
	}

	if (pick && Mouse.GetCondition() == "zone" && this.point1) {
/////////////////////////////////////////ВНИМАНИЕ!! НЕ ПОНИМАЮ, ПОЧЕМУ -2, А НЕ -1////////////////////////////////////
		var point2 = new PointClass(Grid.getXY(pick[0],pick[1])[0]-2, Grid.getXY(pick[0],pick[1])[1]-2);
		console.log(point2);

		var reg = new Region(this.point1.gx,this.point1.gy, point2.gx,point2.gy,randomColor(0.4));
		reg.initialization();
		//console.log(reg);
		Regions.newRegion(reg);

		this.point1 = false;
		Mouse.SetCondition("none");
	}

	if (pick && Mouse.GetCondition() == "DelZone") {
		Mouse.SetCondition("none");
		var ppd = new PointClass(Grid.getXY(pick[0],pick[1])[0]-1, Grid.getXY(pick[0],pick[1])[1]-1);

		Regions.delRegion(ppd.gx, ppd.gy);

		this.point1 = false;
		Mouse.SetCondition("none");
	}

	if (pick && Mouse.GetCondition() == "createWall") {

		//var ppd1 = new PointClass(Grid.getXY(pick[0],pick[1])[0]-1, Grid.getXY(pick[0],pick[1])[1]-1);
		//wallLayer.setWall(ppd1.gx, ppd1.gy, true);
		wallLayer.setWall( Grid.getXY(pick[0],pick[1])[0], Grid.getXY(pick[0],pick[1])[1], true);
		console.log(wallLayer.getWall(Grid.getXY(pick[0],pick[1])[0], Grid.getXY(pick[0],pick[1])[1]));

		this.point1 = false;
		//Mouse.SetCondition("none");
	}

});











//проверка нахождения мыши в boundingBox
function collides(mX, mY, x, y, r, b) {
	if (r >= mX && x < mX && b >= mY && y < mY){
		var nX = mX - x;
		var nY = mY - y;
		return [nX, nY];
	}
	else return false;
}









function blank(){
	ctx.fillStyle = "rgb(100,100,200)";
	ctx.fillRect (0, 0, Grid.x*tileWidht/camera.scale, Grid.y*tileWidht/camera.scale);
}

function background(){
	ctx.drawImage(imgBackground,0,0, Grid.x*tileWidht/camera.scale, Grid.y*tileWidht/camera.scale);
}

function drawRect(reactX, reactY, reactW, reactH, color){
	if (color === undefined) {	ctx.fillStyle = "rgb(255,0,0)"; }
	else {ctx.fillStyle = color; }
	ctx.fillRect (reactX - camera.x, reactY - camera.y, reactW, reactH);
}

function strokeRect(reactX, reactY, reactW, reactH, color){
	//ctx.save();
	if (color === undefined) {ctx.strokeStyle = "black"; ctx.lineWidth = 1;}
	else {ctx.strokeStyle = "red"; ctx.lineWidth = 3;}
	//ctx.restore();
	ctx.strokeRect (reactX - camera.x, reactY - camera.y, reactW, reactH);
}

function randomColor(alpha){

	var rc;

    var r=Math.floor(Math.random() * (256));
    var g=Math.floor(Math.random() * (256));
    var b=Math.floor(Math.random() * (256));

	if (alpha === undefined){
		rc = "rgb("+r+","+g+","+b+")";
	}else{
		rc = "rgba("+r+","+g+","+b+","+alpha+")";
	}

	//console.log(rc);
	return rc;
}












//сетка
function GridClass(xSize,ySize){
	this.x = xSize;
	this.y = ySize;

	//для рисования индекса тайла
	this._indexTile = 0;

	this.array = [];

	this.initialization = function(){
		for (var i = 0; i < xSize; i++) {
 			
			this.array[i] = [];

			for (var j = 0; j < ySize; j++) {
				this.array[i][j] = false;
			}
		}
	};

	//получение типа текустуры по ячейке и сравнение с нужным id
	this.getID = function(xx, yy, id){
		var tr;

		//ОШИБКА ОБРАЩЕНИЯ К НЕСУЩЕСТВУЮЩЕЙ ОБЛАСТИ
		if (xx>0 && xx <= this.x && yy>0 && yy <= this.y) {
			tr = this.array[xx-1][yy-1];
		} else return 0;


		if (id === undefined){
			return tr;
		}

		if (tr == id){
			return 1;
		} else return 0;

	};

	this.setID = function(xx, yy, ID){
		//ОШИБКА ОБРАЩЕНИЯ К НЕСУЩЕСТВУЮЩЕЙ ОБЛАСТИ
		if (xx>0 && xx <= this.x && yy>0 && yy <= this.y) {
			this.array[xx-1][yy-1] = ID;
		}
	};

	//получение ячейки по координатам
	this.getXY = function(xi,yi){
		var xx = Math.ceil( xi/tileWidht );
		var yy = Math.ceil( yi/tileWidht );

		return [xx, yy];
	};

	//получение координат по ячейки
	this.coorXY = function(xi,yi){
		var xx = xi*tileWidht - tileWidht/2;
		var yy = yi*tileWidht - tileWidht/2;

		return [xx, yy];
	};

	//из глобальных координат в середину ячейки
	this.getCoorXY = function(xi1,yi1){
		//получение ячейки по координатам
		var xi = Math.ceil( xi1/tileWidht );
		var yi = Math.ceil( yi1/tileWidht );
		//получение координат по ячейки
		var xx = xi*tileWidht - tileWidht/2;
		var yy = yi*tileWidht - tileWidht/2;

		return [xx, yy];
	};



	//получение индекса текстуры в тайлсете по ячейке
	this.idTileGet = function (xx, yy, id){

		var north_tile, south_tile, west_tile, east_tile, north_west_tile, north_east_tile, south_west_tile, south_east_tile;

		north_tile = this.getID(xx, yy-1, id);
		south_tile = this.getID(xx, yy+1, id);
		west_tile = this.getID(xx-1, yy, id);
		east_tile = this.getID(xx+1,yy, id);

		north_west_tile = this.getID(xx-1, yy-1, id);
		north_east_tile = this.getID(xx+1, yy-1, id);
		south_west_tile = this.getID(xx-1, yy+1, id);
		south_east_tile = this.getID(xx+1, yy+1, id);

		var index = north_west_tile + 2*north_tile + 4*north_east_tile + 8*west_tile + 16*east_tile + 32*south_west_tile + 64*south_tile + 128*south_east_tile;

		this._indexTile = index;

		return getTile[ index ];

	};


	this.update = function(){

		for (var j = 0; j < this.y; j++) {
			for (var i = 0; i < this.x; i++) {

				//рисуем граунд
				var xq = 32;
				var yq = 0;

				var xv = i*tileWidht - camera.x;
				var yv = j*tileWidht - camera.y;

				ctx.drawImage(imgGround, xq, yq, tileWidht, tileWidht, 
						xv, yv, tileWidht, tileWidht);


				if (this.getID(i+1,j+1,1)){
					var typeTextureTile = this.idTileGet (i+1,j+1, 1);


					//ЖЕЛАТЕЛЬНО УБРАТЬ ЭТУ ФУНКЦИЮ ИЗ АПДЕЙТА/////////////
					LayerCollision.setCollision(i+1,j+1,true);
					//console.log((i+1) + "  " + (j+1));


					//рисуем тайлы
					ctx.drawImage(img, idTile[typeTextureTile][1], idTile[typeTextureTile][0], tileWidht, tileWidht, 
						xv, yv, tileWidht, tileWidht);

					//идентификатор тайла
					/*
					ctx.fillStyle="rgb(0,0,0)";
					ctx.textAlign = "center";
					ctx.font = "20px Calibri";
  					ctx.fillText(this._indexTile, xv+15, yv+20);*/
				}

			}
		}

	};
}


var Grid = new GridClass(25,20);

/////БАГ//////
//var Grid = new GridClass(20,10);
Grid.initialization();

//рисуем сетку
function drawGrid(){
	for (var j = 0; j < Grid.y; j++) {
 		
		for (var i = 0; i < Grid.x; i++) {
			strokeRect(i*tileWidht, j*tileWidht, tileWidht, tileWidht);
		}

	}
}









//////////////////Элипс//////////////////////
function ellips(x,y,w,h) {
	w=Number(w);
	h=Number(h);

	ctx.save();
	ctx.beginPath();
	// Переносим систему координат (СК) в центр будущего эллипса
	ctx.translate(x + w/2, y + h/2);
	/*
 	* Масштабируем по х.
	* В результате окружность вытянется в a / b раз
	* и станет эллипсом
	*/
	ctx.scale((w/2) / (h/2), 1);
	// Рисуем окружность, которая благодаря масштабированию станет эллипсом
	ctx.arc(0, 0, h/2, 0, Math.PI * 2, true);
	ctx.closePath();

	// Задаём стили, выводим контур и закрашенный эллипс на холст
	ctx.lineWidth = 2;
	ctx.strokeStyle = "rgba(0,255,0,1.0)";
	ctx.fillStyle = "rgba(0,150,150,1.0)";
	ctx.stroke();
	ctx.fill();

	// Восстанавливаем СК и масштаб
	ctx.restore();
}


var Selected = {
	selObj : false,

	initialization: function(){
		selObj = false;
	},

	setSelected: function(entity){
		selObj = entity;
	},

	getSelected: function(){
		return selObj;
	},

	delSelected: function(){
		selObj = false;
	},


	update: function(){
		if (selObj) {
///////////////////////////////ОТРИСОВКА КРУГА ВЫБОР///////////////////////////////
			//ellips(selObj.x - camera.x-16,selObj.physY+2 - camera.y,64,32);
		}
	}


};
Selected.initialization();











/////////////////////СЛОИ///////////////////////////
var Layer = {	
	arrayObj : [],

	sortY: function(a,b){
		if (a.physY > b.physY) return 1;
 		if (a.physY < b.physY) return -1;
	},

	sortX: function(a,b){
		if (a.physX > b.physX) return 1;
 		if (a.physX < b.physX) return -1;
	},

	update : function(){

		this.arrayObj.sort(this.sortX);
		this.arrayObj.sort(this.sortY);

		for (var i = 0; i < this.arrayObj.length; i++) {
 				this.arrayObj[i].update();  			
		}
	},

	create : function(xx, yy, src, imgX, imgY, width, height, type){
		var img = new Image();
		img.src = src; // Устанавливает путь

			//var xt = xx+ Math.ceil(camera.x);
			//var yt = yy+ Math.ceil(camera.y);



////////////////////////////////////ВНИМАНИЕ ТУТ БАГ////////////////////////////////////////
//////////////////////КЛЕТКА С КОЛЛИЗИЕЙ ПРОСЧИТЫВАЕТСЯ НЕ ДОСТОВЕРНО///////////////////////
/////////////////////////+1 ЭТО УЛОВКА, ЧТОБЫ ИЗБЕЖАТЬ БАГ//////////////////////////////////



		var xt = xx+ camera.x + 1;
		var yt = yy+ camera.y + 1;







		this.arrayObj.push( new Sprite(xt, yt, img, imgX, imgY, width, height) );

		if (type == "Tree"){
			LayerCollision.setCollision( Grid.getXY(xt, yt)[0]+1, Grid.getXY(xt, yt)[1]+4, true );
			LayerCollision.setCollision( Grid.getXY(xt, yt)[0]+2, Grid.getXY(xt, yt)[1]+4, true );

			console.log( (xt)+ "  " +  (yt));

		}

	},

	add : function (AnimationSprite){
		this.arrayObj.push(AnimationSprite);
	},

	length : function (){
		return this.arrayObj.length;
	},

	getObj : function(i){
		return this.arrayObj[i];  
	}
};

var LayerCollision = {	
	arrayMap : [],

	initialization : function(){
		for (var i = 0; i <= Grid.x; i++) {
 			
			this.arrayMap[i] = [];

			for (var j = 0; j <= Grid.y; j++) {
				if (Grid.getID(i+1,j+1,1)) {this.arrayMap[i][j] = true;}
				else this.arrayMap[i][j] = false;
			}
		}
	},

	setCollision : function(xx, yy, condition){
		if (xx>0 && xx <= Grid.x && yy>0 && yy <= Grid.y) {
			this.arrayMap[xx][yy] = condition;
		}
	},

	getCollision : function(xx, yy){

		//ОШИБКА ОБРАЩЕНИЯ К НЕСУЩЕСТВУЮЩЕЙ ОБЛАСТИ
		if (xx>0 && xx <= Grid.x && yy>0 && yy <= Grid.y) {
			return this.arrayMap[xx][yy];
		} else return true;
	},

	update: function(){

		for (var j = 0; j < Grid.y; j++) {
 		
			for (var i = 0; i < Grid.x; i++) {

				var c1 = "rgba(150,0,0,0.5)";
				var c2 = "rgba(50,150,150,0.3)";

///////////////////////////////ОТРИСОВКА ПРОХОДИМОСТИ///////////////////////////////
				//if (this.getCollision(i+1,j+1)) drawRect(i*tileWidht, j*tileWidht, tileWidht, tileWidht, c1);
				//else drawRect(i*tileWidht, j*tileWidht, tileWidht, tileWidht, c2);
			}

		}

	}
};

LayerCollision.initialization();







var wallLayer = {
	arr : [],
	x: 0,
	y: 0,

	initialization : function(){
		for (i = 0; i <= Grid.x; i++) {
 			
			this.arr[i] = [];
			this.x = i;

			for (j = 0; j <= Grid.y; j++) {
				//if (Grid.getID(i+1,j+1,1)) {this.arr[i][j] = true;}
				//else 
				this.arr[i][j] = false;
				this.y = j;
			}
		}
	},

	setWall : function(xx,yy,bool){
		//добавляем "крышу"
		if (xx>0 && xx <= this.x && yy>0 && yy <= this.y) { //ошибка обращения к несуществующей области
			//if (!LayerCollision.getCollision(xx-1,yy-1)){  //проверка на проходимость
				this.arr[xx-1][yy-1]=bool;
				LayerCollision.setCollision(xx,yy,true);
			//}
		}

	},

	getWall : function(xx,yy){
		//ОШИБКА ОБРАЩЕНИЯ К НЕСУЩЕСТВУЮЩЕЙ ОБЛАСТИ
		if (xx>0 && xx <= this.x && yy>0 && yy <= this.y) {
			return this.arr[xx-1][yy-1];
		} else return 0;

	},

	//получение индекса текстуры в тайлсете по ячейке
	getTile : function (xx, yy){

		var north_tile, south_tile, west_tile, east_tile, north_west_tile, north_east_tile, south_west_tile, south_east_tile;

		north_tile = this.getWall(xx, yy-1);
		south_tile = this.getWall(xx, yy+1);
		west_tile = this.getWall(xx-1, yy);
		east_tile = this.getWall(xx+1,yy);

		north_west_tile = this.getWall(xx-1, yy-1);
		north_east_tile = this.getWall(xx+1, yy-1);
		south_west_tile = this.getWall(xx-1, yy+1);
		south_east_tile = this.getWall(xx+1, yy+1);

		var index = north_west_tile + 2*north_tile + 4*north_east_tile + 8*west_tile + 16*east_tile + 32*south_west_tile + 64*south_tile + 128*south_east_tile;

		return getTile[ index ];

	},

	update: function(){
		for (j = 0; j < this.y; j++) {
			for (i = 0; i < this.x; i++) {

				var xv = i*tileWidht - camera.x;
				var yv = j*tileWidht - camera.y;

				if (this.getWall(i+1,j+1)){
					//console.log("!");


					var typeTextureTile = this.getTile (i+1,j+1);
					//LayerCollision.setCollision(i+1,j+1,true);


					//рисуем тайлы
					ctx.drawImage(imgWallRoof, idTile[typeTextureTile][1], idTile[typeTextureTile][0], tileWidht, tileWidht, 
						xv, yv, tileWidht, tileWidht);

				}


			}
		}
	}
};


wallLayer.initialization();/*
wallLayer.setWall(10,10,true);
wallLayer.setWall(11,10,true);
wallLayer.setWall(12,10,true);*/
//console.log("wallLayer");

































function PointClass(xx,yy){
	this.gx = xx;	//x ячейки
	this.gy = yy;	//y ячейки

	this.x = Grid.coorXY(xx,yy)[0];
	this.y = Grid.coorXY(xx,yy)[1];

	this.F = 0; //для расчета поиска пути
	this.G = 0; //для расчета поиска пути
	this.H = 0; //для расчета поиска пути
	this.exodus = 0; //родительская точка, откуда пришли //для поиска пути
	this.check = 0; //open || close


	this.set = function(xxx,yyy){

		this.gx = xxx;	//x ячейки
		this.gy = yyy;	//y ячейки

		this.x = Grid.getCoorXY(xxx,yyy)[0];
		this.y = Grid.getCoorXY(xxx,yyy)[1];


	};
}



var Orders = {
	arr: [],

	newPoint: function(point){
		this.arr.push(point);
	},

	delPoint: function(i){
		this.arr.splice(i,1);
	},

	getPoint: function(i){
		return this.arr[i];
	},

	update: function(){
			for (var i = 0; i <=  this.arr.length; i++) {
/////////////////////////////////////////ОТРИСОВКА КРУГА ВЫБОРА////////////////////////////////////////////
				//if (this.arr[i]) ellips(this.arr[i].x - camera.x-16, this.arr[i].y+2 - camera.y,64,32);
		}
	},

	length: function(){
		return this.arr.length;
	}
};



function Path(start, end, entity){
	this.Entity = entity;
	this.arr = [];
	this.start = start;
	this.end = end;
	this.currentPoint = 0;
	this.complete = false;

	this.newList = []; //для поиска пути

	this.newPoint = function(point){
		this.arr.push(point);
	};

	this.delPoint = function(i){
		this.arr.splice(i,1);
	};

	this.popPoint = function(){ //удалить последний элемент
		//this.arr.pop();
		this.arr.splice(this.arr.length-1,1);
	};

	this.getPoint = function(i){
		return this.arr[i];
	};

	this.getEndPoint = function(){
		return this.arr[ this.arr.length-1 ];
	};

	this.getStartPoint = function(){
		return this.arr[0];
	};

	this.length = function(){
		return this.arr.length;
	};

	this.findPathTest = function(){
		var myx = this.end.gx - this.start.gx;
		var myy = this.end.gy - this.start.gy;

		var zx = 0;

		if (myx > 0){
			for (var ix = 0; ix < myx; ix++) {
				var tt = new PointClass(this.start.gx + ix+1, this.start.gy);
				this.newPoint(tt);
				zx = ix+1;
				//console.log (tt);
			}
		}
		
		
		if (myx < 0){
			for (var ix1 = 0; ix1 > myx; ix1--) {
				var tt1 = new PointClass(this.start.gx + ix1-1, this.start.gy);
				this.newPoint(tt1);
				zx = ix1-1;
			}
		}

		if (myy > 0){
			for (var iy = 0; iy < myy; iy++) {
				var tt2 = new PointClass(this.start.gx + zx, this.start.gy + iy);
				this.newPoint(tt2);
			}
		}
		if (myy < 0){
			for (var iy1 = 0; iy1 > myy; iy1--) {
				var tt3 = new PointClass(this.start.gx + zx, this.start.gy + iy1);
				this.newPoint(tt3);
			}
		}

		this.newPoint(this.end);

	};


	this.H = function(point){ //от точки до цели
		var h1 = this.end.gx - point.gx;
		var h2 = this.end.gy - point.gy;
		var h3 = (Math.abs(h1) + Math.abs(h2))*10;
		return h3;
	};


	this.F = function(point, exodus){ //общая стоимость
		var H1 = this.H(point);
		point.F = point.G+H1;
		point.H = H1;
		point.exodus = exodus;
		//return G+H;
	};

	this.sortF = function(a,b){
		if (a.F > b.F) return 1; //у кого F больше - тот в конце списка
 		if (a.F < b.F) return -1;
	};

	this.createNewList = function(){ //для поиска пути

		for (var j = 0; j < Grid.y; j++) {
 		
			for (var i = 0; i < Grid.x; i++) {

				var pcti = new PointClass(i, j);
				if (pcti.gx == this.start.gx && pcti.gy == this.start.gy) pcti = false; //мы стартовую точку и так добавляем
				if (pcti) this.newList.push(pcti);
			}

		}

	};

	this.getNewList = function(xx43,yy43){
		for (var i43 = 0; i43 < this.newList.length; i43++) {
			if (this.newList[i43].gx == xx43 && this.newList[i43].gy == yy43)
				return this.newList[i43];
		}
		return false;
	};

	this.findPath = function(){
		this.createNewList();


		var openList = [];
		var closedList = [];

		this.F(this.start,0);
		openList.push(this.start);


		while (true) {

			if (openList.length === 0) break;
			openList.sort(this.sortF);
			if (openList[0].gx == this.end.gx && openList[0].gy == this.end.gy) {console.log("find Point!!!"); break;}

			var pct = this.getNewList(openList[0].gx+1, openList[0].gy);
			////////////////////////////////////////////////////var pct = this.getNewList(4, 4);//////////////////////////////////////////////////////
			if(pct && !LayerCollision.getCollision(pct.gx, pct.gy) && pct.check != "open" && pct.check != "close"){
				pct.G = openList[0].G+10;
				this.F(pct, openList[0]);
				pct.check = "open";
				openList.push(pct);
			}
			var pct1 = this.getNewList(openList[0].gx, openList[0].gy+1);
			if(pct1 && !LayerCollision.getCollision(pct1.gx, pct1.gy) && pct1.check != "open" && pct1.check != "close"){
				pct1.G = openList[0].G+10;
				this.F(pct1, openList[0]);
				pct1.check = "open";
				openList.push(pct1);
			}
			var pct2 = this.getNewList(openList[0].gx-1, openList[0].gy);
			if(pct2 && !LayerCollision.getCollision(pct2.gx, pct2.gy) && pct2.check != "open" && pct2.check != "close"){
				pct2.G = openList[0].G+10;
				this.F(pct2, openList[0]);
				pct2.check = "open";
				openList.push(pct2);
			}
			var pct3 = this.getNewList(openList[0].gx, openList[0].gy-1);
			if(pct3 && !LayerCollision.getCollision(pct3.gx, pct3.gy) && pct3.check != "open" && pct3.check != "close"){
				pct3.G = openList[0].G+10;
				this.F(pct3, openList[0]);
				pct3.check = "open";
				openList.push(pct3);
			}


			//ХЗ зачем это
			if(pct && !LayerCollision.getCollision(pct.gx, pct.gy) && pct.check == "open" && (openList[0].G+10)<pct.G){

				pct.G = openList[0].G+10;
				this.F(pct, openList[0]);
				pct.check = "close";
				closedList.push(pct);
			}
			if(pct1 && !LayerCollision.getCollision(pct1.gx, pct1.gy) && pct1.check == "open" && (openList[0].G+10)<pct1.G){

				pct1.G = openList[0].G+10;
				this.F(pct1, openList[0]);
				pct1.check = "close";
				closedList.push(pct1);
			}
			if(pct2 && !LayerCollision.getCollision(pct2.gx, pct2.gy) && pct2.check == "open" && (openList[0].G+10)<pct2.G){

				pct2.G = openList[0].G+10;
				this.F(pct2, openList[0]);
				pct2.check = "close";
				closedList.push(pct2);
			}
			if(pct3 && !LayerCollision.getCollision(pct3.gx, pct3.gy) && pct3.check == "open" && (openList[0].G+10)<pct3.G){

				pct3.G = openList[0].G+10;
				this.F(pct3, openList[0]);
				pct3.check = "close";
				closedList.push(pct3);
			}


			openList[0].check = "close";
			closedList.push(openList[0]);
			openList.shift();


		}


		var currentPoint = openList[0];

		while (true) {


			currentPoint = currentPoint.exodus;
			this.newPoint(currentPoint);

			if (currentPoint.gx == this.start.gx && currentPoint.gy == this.start.gy) {console.log("find Path!!!"); break;}

		}

	};






	this.checkin = function(point){
		if (this.Entity && this.Entity.Point.gx == point.gx && this.Entity.Point.gy == point.gy) return true;
		else return false;
	};

	this.paintPath = function(start,end){
		ctx.strokeStyle = "green"; 
		ctx.lineWidth = 1;
		ctx.beginPath();
  	  	ctx.moveTo(start.x-camera.x,start.y-camera.y);
  	 	ctx.lineTo(end.x-camera.x,end.y-camera.y);
   		ctx.closePath();
   		ctx.stroke();
	};

	this.update = function(){


			if (this.checkin(this.getEndPoint()) && this.currentPoint == this.length()) {
				this.complete = true;
			} else {
				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
				var point1 = this.getPoint(this.currentPoint);
				if(this.Entity) this.Entity.moveTo(point1);
				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1

///////////////////////////////ОТРИСОВКА ПУТИ///////////////////////////////
				for (var i1 = this.currentPoint; i1 < this.length(); i1++){
					if (this.getPoint(i1+1)) this.paintPath(this.getPoint(i1), this.getPoint(i1+1));
				}


				if (this.checkin(this.getPoint(this.currentPoint))) {

					//удаление точки из массива orders
					for (var i = 0; i <= Orders.length(); i++) {
						if (Orders.getPoint(i) == this.getPoint(this.currentPoint)) Orders.delPoint(i);
					}

					this.currentPoint++; //ТУТ ВОЗМОЖНА ЗАДЕРЖКА В 1 КАДР!!!!!!!!!!!!!!11
				}


			}
	};
}




var Paths = {
	arr: [],

	newPath: function(path){
		this.arr.push(path);
	},

	delPath: function(i){
		this.arr.splice(i,1);
	},

	getPath: function(i){
		return this.arr(i);
	},

	update: function(){
		for (var i = 0; i <=  this.arr.length; i++) {
			if (this.arr[i]) this.arr[i].update();
				if (this.arr[i] && this.arr[i].complete) {

					console.log("END PATH!!!!");
					Selected.getSelected().goal = 0;
					Selected.delSelected();
					Mouse.SetCondition("none");




					this.delPath(i);

				}
		}
	},

	length: function(){
		return this.arr.length;
	}
};









////////////////////////////////////////////////////////////////////////////////////
/*
Mixin.Observable = {
    _init: function(){
        this.__observers = {};
    },
    attach: function(name, observer){
        if( !this.__observers[name] ) this.__observers[name] = [];
        this.__observers[name].include(observer);
    },
    detach: function(name, observer){
        if( this.__observers[name] ) this.__observers[name].erase(observer);
    },
    notify: function(name){
        var args = [];
        for( var i=1; i < arguments.length; i++ )
            args.push(arguments[i]);
        if( this.__observers[name] ) this.__observers[name].each(function(observer){
            observer.apply(null, args);
        });
    }
};
*/

//МИКСИН СОБЫТИЯ
var eventMixin = {

  /**
   * Подписка на событие
   * Использование:
   *  menu.on('select', function(item) { ... }
  */
  on: function(eventName, handler) {
    if (!this._eventHandlers) this._eventHandlers = {};
    if (!this._eventHandlers[eventName]) this._eventHandlers[eventName] = [];
    this._eventHandlers[eventName].push(handler);
  },

  /**
   * Прекращение подписки
   *  menu.off('select',  handler)
   */
  off: function(eventName, handler) {
    var handlers = this._eventHandlers && this._eventHandlers[eventName];
    if (!handlers) return;
    for(var i=0; i<handlers.length; i++) {
      if (handlers[i] == handler) {
        handlers.splice(i--, 1);
      }
    }
  },

  /**
   * Генерация события с передачей данных
   *  this.trigger('select', item);
   */
  trigger: function(eventName /*, ... */) {

    if (!this._eventHandlers || !this._eventHandlers[eventName]) {
      return; // обработчиков для события нет
    }

    // вызвать обработчики
    var handlers = this._eventHandlers[eventName];
    for (var i = 0; i < handlers.length; i++) {
      handlers[i].apply(this, [].slice.call(arguments, 1));
    }

  }
};

//добавление функционала событий к классу fn
function addEventMixin(fn){
	for(var key in eventMixin) {
  		fn.prototype[key] = eventMixin[key];
	}
}










function TriggerClass(){

	this.on("left", function(value){
		Event1.cons(value);
	});

	this.on("exit", function(value){
		console.log("выход из зоны № " + value);
	});
	this.on("enter", function(value){
		console.log("вход в зону № " + value);
		//Door.trigger();
	});
	this.on("into", function(value){
		console.log("нахождение в зоне № " + value);
	});
}
addEventMixin(TriggerClass);
var Trigger = new TriggerClass();


function EventClass(){
	this.cons = function(value) {
  		console.log("Выбрано значение " + value);
	};

	function _lala(val){
		console.log("Выбрано2 " + val);
	}

	Trigger.on("left", function(value2){
		_lala(value2);
	});
}
var Event1 = new EventClass();


//var Event1 = new EventClass();
/*Event1.on("left", function(value) {
  		console.log("Выбрано значение " + value);
	});
console.log(Event1);*/

//////////////////////////////////////////////////////////////////////////






function Region (x1,y1,x2,y2, color){	
	//this.arr = [];
	this.id = 0;
	this.color = "";
	this.width = 0;
	this.height = 0;
	this.x = 0;
	this.y = 0;
	this.event = "none";
	this.color = color;
	this._enter = false;


	this.initialization = function(){
		if (x2>=x1) {this.width = x2-x1; this.x = x1;} else {this.width = x1-x2; this.x = x2;}
		if (y2>=y1) {this.height = y2-y1; this.y = y1;} else {this.height = y1-y2; this.y = y2;}
		//if (this.collision) this._enter = true;
	};

	//initialization();

	this.paint = function(){
		for (var j = 0; j < Grid.y; j++) {
 		
			for (var i = 0; i < Grid.x; i++) {

///////////////////////////////ОТРИСОВКА ЗОН///////////////////////////////
				if ( collides(i,j, this.x, this.y, this.x+this.width+1, this.y+this.height+1) ) 
					drawRect(i*tileWidht, j*tileWidht, tileWidht, tileWidht, this.color);
			}

		}
	};

	this.collision = function(){
		return collides(Hero.Point.gx, Hero.Point.gy, this.x, this.y, this.x+this.width+1, this.y+this.height+1);
	};

	this.update = function(){
		this.paint();
		if (this.collision() && !this._enter) {
			this.event = "enter"; 
			this._enter = true;
			Trigger.trigger(this.event, this.id);
		}

		if (this.collision() && this._enter) {
			this.event = "into";
			Trigger.trigger(this.event, this.id);
		}

		if (!this.collision() && this._enter) {
			this.event = "exit";
			this._enter = false;
			Trigger.trigger(this.event, this.id);
		}

	};
}

var Regions = {
	arr: [],
	_currentID: 0,

	newRegion: function(reg){
		if (!this.currentID) this.currentID = 0;
		reg.id = this.currentID;
		this.currentID++;
		this.arr.push(reg);
	},

	delRegion: function(x,y){
		
		for (var i2 = 0; i2 <=  this.arr.length; i2++) {
			if (collides(x,y, this.arr[i2].x, this.arr[i2].y, this.arr[i2].x+this.arr[i2].width+1, this.arr[i2].y+this.arr[i2].height+1)) this.arr.splice(i2,1);
		}

	},

	getRegion: function(id){

		for (var i1 = 0; i1 <=  this.arr.length; i1++) {
			if (this.arr[i1].id == id) return this.arr(i1);
		}
	},

	update: function(){
		for (var i = 0; i <=  this.arr.length; i++) {
			if (this.arr[i]) this.arr[i].update();

		}
	},

	length: function(){
		return this.arr.length;
	}
};
/*
var reg1 = new Region(5,5,15,15, randomColor(0.4));
reg1.initialization();
//console.log(reg1);
Regions.newRegion(reg1);

var reg2 = new Region(2,2,6,6, randomColor(0.4));
reg2.initialization();
//console.log(reg2);
Regions.newRegion(reg2);
*/









////////////////////ЭТО КЛАССЫ, А НЕ ЭКЗЕМЛПЯРЫ///////////////////
//Игровой объект
function Entity(xx, yy){
	this.x = Grid.coorXY(xx,yy)[0] - 16;
	this.y = Grid.coorXY(xx,yy)[1] - 32;
	this.orientation = "down";
	this.isWalk = false;

	//физические позиции
	this.physX = this.x+16;
	this.physY = this.y+32;



	this.speed = 3;



	this.goal = 0;
	this.Point = new PointClass(Grid.getXY(this.x, this.y)[0], Grid.getXY(this.x, this.y)[1]); //обновляем местоположение

	this.paintPath = function(goal){
		ctx.strokeStyle = "green"; 
		ctx.lineWidth = 1;
		ctx.beginPath();
  	  	ctx.moveTo(this.physX-camera.x,this.physY-camera.y);
  	 	ctx.lineTo(goal.x-camera.x,goal.y-camera.y);
   		ctx.closePath();
   		ctx.stroke();
	};

	this.moveTo = function(goal){
		if (this.x != goal.x || this.y != goal.y){
			this.goal = goal;

			this.translate(this.goal.x - this.x, this.goal.y - this.y);
			/////////////////ВНИМАНИЕ/////////////
			this.paintPath(this.goal);



		} else {
			//////////////////////////ВАЖНО//////////////////////
			//this.goal = 0;
			//Selected.delSelected();
			//Mouse.SetCondition("none");
		}
	};


	this.stop = function(){
		this.goal = 0;
			Selected.delSelected();
			Mouse.SetCondition("none");
	};





	this.update = function(){

		if (this.goal !==0){this.isWalk = true;} else {this.isWalk = false;}

		//физические позиции
		this.physX = this.x+16;
		this.physY = this.y+32;

		//перебираем все свойства этого объекта
		for (var key in this) {
 			//!!!!!!!!!!!!!!!!!!!!!!
 			//if (this.hasOwnProperty(key)) continue; //если свойство свое - сразу пропускаем
 			if (this[key].updated) {
 				this[key].update();  			
 				//console.log(this[key]);
 			} //если внутренний объект имеет свойство updated, то вызываем метод апдейта

		}

		var ff = Grid.getXY(this.x, this.y);
		//console.log(ff);		
		this.Point.set(ff[0], ff[1]); //обновляем местоположение
	};



	this.translate = function(xx,yy){
		var vert = 0;
		var hor = 0;

		var xxx = 0;
		var yyy = 0;

			////////НАДО ПЕРЕДЕЛАТЬ НА СЛОЙ С КОЛЛИЗИЯМИ///////////
		if (xx>0){		//проверка столкновений и край карты
			hor = !LayerCollision.getCollision(this.getXY(this.physX-16, this.physY)[0]+1, this.getXY(this.physX-16, this.physY)[1]);
			xxx = (xx < this.speed && this.goal !==0) ? (this.goal.x - this.x) : this.speed;
			this.orientation = "right";

			//xxx = this.speed;
		}
		if (xx<0){		//проверка столкновений и край карты
			hor = !LayerCollision.getCollision(this.getXY(this.physX+16, this.physY)[0]-1, this.getXY(this.physX+16, this.physY)[1]);
			xxx = (xx > -this.speed && this.goal !==0) ? (this.goal.x - this.x) : -this.speed;
			this.orientation = "left";

			//xxx = - this.speed;
		}
		if (xx === 0){
			hor = 0;
			xxx = 0;
		}


		if (yy>0){		//проверка столкновений и край карты
			vert = !LayerCollision.getCollision(this.getXY(this.physX, this.physY-16)[0], this.getXY(this.physX, this.physY-16)[1]+1);
			yyy = (yy < this.speed && this.goal !==0) ? (this.goal.y - this.y) : this.speed;
			this.orientation = "down";

			//yyy = this.speed;
		}

		if (yy<0){		//проверка столкновений и край карты
			vert = !LayerCollision.getCollision(this.getXY(this.physX, this.physY+30)[0], this.getXY(this.physX, this.physY+30)[1]-1);
			yyy = (yy > -this.speed && this.goal !==0) ? (this.goal.y - this.y) : -this.speed;
			this.orientation = "up";

			//yyy = -this.speed;
		}
		if (yy ===0){	
			vert = 0;
			yyy = 0;
		}



		this.x += xxx*hor;
		this.y += yyy*vert;
	};

	//получение ячейки по координатам
	this.getXY = function(xi,yi){
		var xx = Math.ceil( xi /tileWidht );
		var yy = Math.ceil( yi /tileWidht );

		return [xx, yy];
	};
}

//Анимация
function AnimationSprite(Sprite, Entity){	//Entity - родитель
	//this.updated = false;
	this._AnimationMassive = {"down":[0,0,32,64,96], "left": [48,0,32,64,96], "right": [96,0,32,64,96], "up": [144,0,32,64,96]};
	this._AnimationSpeed = 10;
	this._cadr = 1;

	this.Entity = Entity;

	this.physX = this.Entity.physX;
	this.physY = this.Entity.physY;



	this.bbx = this.physX-16;
	this.bby = this.physY+128;
	this.bbw = 32;
	this.bbh = 48;

	this.update = function(){

		this.physX = this.Entity.physX;
		this.physY = this.Entity.physY-160;


		if (this.Entity.isWalk){
			this._AnimationSpeed--;

			if (this._AnimationSpeed <= 0) {
				this._cadr++;
				this._AnimationSpeed = 10;
			}
			if (this._cadr >= 5 || this._cadr <=0) {
				this._cadr = 1;
				this._AnimationSpeed = 10;
			}

		} else this._cadr = 1;

		//ctx.globalAlpha = 0.5;
		ctx.drawImage(Sprite, this._AnimationMassive[ this.Entity.orientation ][ this._cadr ], this._AnimationMassive[ this.Entity.orientation ][0], 32, 48, 
			/*/////////////////////ЛОКАЛЬНЫЕ КООРДИНАТЫ С КАМЕРОЙ////////////////////*/this.Entity.x - camera.x, this.Entity.y - camera.y, 32, 48);
		//ctx.globalAlpha = 1;


		this.bbx = this.physX-16;
		this.bby = this.physY+128;

///////////////////////////////ОТРИСОВКА BOUNDING BOX///////////////////////////////
		//strokeRect(this.bbx, this.bby, this.bbw, this.bbh, "red");

	};
}

//Контроллер Игрока
function Controller(Entity){	//Entity - родитель
	this.updated = true;

	this.update = function(){
		if (isKeyDown("Left") && !isKeyDown("Right")){
			Entity.stop();
			Entity.orientation = "left";
			Entity.isWalk = true;

			Entity.translate(-1,0);

/////////////////////////////////////////////ТЕСТ МИКСИНА СОБЫТИЙ/////////////////////////////////////////////////
			//Trigger.trigger("left", 10);
		}

		if (isKeyDown("Right") && !isKeyDown("Left")){
			Entity.stop();
			Entity.orientation = "right";
			Entity.isWalk = true;

			Entity.translate(1,0);
		}

		if (isKeyDown("Up") && !isKeyDown("Down")){
			Entity.stop();
			Entity.orientation = "up";
			Entity.isWalk = true;

			Entity.translate(0,-1);
		}

		if (isKeyDown("Down")  && !isKeyDown("Up")){
			Entity.stop();
			Entity.orientation = "down";
			Entity.isWalk = true;

			Entity.translate(0,1);
		}


		if (isKeyDown("G")){
			Entity.stop();
		}

		if ( !isKeysDown(["Left", "Right", "Up", "Down"]) &&  Entity.goal ===0 ) Entity.isWalk = false;
	};
}
//////////////////////////////////////////////////////////////////
function Create(){
	this.x=0;
	this.y=0;
	this.opacity = 0.5;
	this.on = false;

	this.img = 0;
	this.imgX = 0;
	this.imgY = 0;
	this.height = 0;
	this.width = 0;

	this.update = function(){
		if(this.on){

			this.getXY();

			ctx.globalAlpha = this.opacity;
			ctx.drawImage(this.img, this.imgX, this.imgY, this.width, this.height, this.x, this.y, this.width, this.height);
			ctx.globalAlpha = 1;
		}
	};

	this.getXY = function(){ //расставляем объект по сетке
			//var oo = Grid.getCoorXY (Math.ceil(Mouse.x - camera.x), Math.ceil(Mouse.y - camera.y));

			//var oo = Grid.getCoorXY (Mouse.x - camera.x, Mouse.y - camera.y);

			//this.x = oo[0] - Math.ceil(this.width/2);
			//this.y = oo[1] - Math.ceil(this.height/2);

			var oo = Grid.getXY(Mouse.x, Mouse.y);

//////////////////////////////////////////////////////////НЕ УДАЛЯТЬ/////////////////////////////////////////////////////////
///////////////////////////////////////ТУТ СКОРЕЕ ВСЕГО БАГ ИЗ-ЗА ТОГО, ЧТО СЛИШКОМ РАНО/////////////////////////////////////
///////////////////////////////////////////////////ПРОСЧИТЫВАЮ ПОЛОЖЕНИЕ КАМЕРЫ//////////////////////////////////////////////

			//var oo = Grid.getXY(Mouse.x - camera.x, Mouse.y - camera.y);


			this.x = oo[0]*tileWidht - camera.x - tileWidht*2;
			this.y = oo[1]*tileWidht - camera.y - tileWidht*2;
	};

	this.delete = function(){
		this.on = false;

		this.img = 0;
		this.imgX = 0;
		this.imgY = 0;
		this.height = 0;
		this.width = 0;
	};

	this.new = function(src, xx, yy, width, height){
		this.on = true;
		
		this.img = new Image();
		this.img.src = src;
		this.imgX = xx;
		this.imgY = yy;
		this.height = height;
		this.width = width;
	};
}

/////////////////////////////////ФАБРИКА ОБЪЕКТОВ////////////////////////////////////////
function Sprite(xx, yy, img, imgX, imgY, width, height){
	this.x = xx;
	this.y = yy;

	this.physX = this.x;
	this.physY = this.y;

	this.img = img;

	this.imgX = imgX;
	this.imgY = imgY;
	this.width = width;
	this.height = height;

	this.bbx = this.x;
	this.bby = this.y;
	this.bbw = 128;
	this.bbh = 160;

	this.update = function(){
		ctx.drawImage(this.img, this.imgX, this.imgY, this.width, this.height, this.x - camera.x, this.y - camera.y, this.width, this.height);

		this.bbx = this.x;
		this.bby = this.y;

///////////////////////////////ОТРИСОВКА BOUNDING BOX///////////////////////////////
		//strokeRect(this.bbx, this.bby, this.bbw, this.bbh, "red");
	};
}







var img1 = new Image();   // Создает новый элемент изображения
img1.src = './img/character.png'; // Устанавливает путь

var Hero = new Entity (5,5);
var AnimationSprite1 = new AnimationSprite(img1, Hero);
Hero.AnimationSprite = AnimationSprite1;
Layer.add(Hero.AnimationSprite);

Hero.Controller = new Controller(Hero);

var Create1 = new Create();

//Selected.setSelected(Hero);

/*
Hero.Controller.on("left", function(value) {
  		console.log("Выбрано значение " + value);
	});
console.log(Hero.Controller);*/











function DoorClass(xx,yy){
	this.x = Grid.coorXY(xx,yy)[0] - 16;
	this.y = Grid.coorXY(xx,yy)[1] - 16;
	this.condition = "close";
	this.boolAnim = false;


	this._AnimationMassive = [0,32,64,96];
	this._AnimationSpeed = 10;
	this._cadr = 0;

	this.trigger = function(){
		this.boolAnim = true;

		if (this.condition == "close"){
			this._AnimationSpeed--;

			if (this._AnimationSpeed <= 0) {
				this._cadr++;
				this._AnimationSpeed = 10;
			}

			if (this._cadr >= 4) {
				this._AnimationSpeed = 10;
				this.condition = "open";
				this.boolAnim = false;
				LayerCollision.setCollision(xx,yy, false);
			}
		} else {
			this._AnimationSpeed--;

			if (this._AnimationSpeed <= 0) {
				this._cadr--;
				this._AnimationSpeed = 10;
			}

			if (this._cadr <= 0) {
				this._AnimationSpeed = 10;
				this.condition = "close";
				this.boolAnim = false;
				LayerCollision.setCollision(xx,yy, true);
			}
		}

	};

	this.update = function(){
		if (this.boolAnim)
			this.trigger();

		ctx.drawImage(doorImg, 0, this._AnimationMassive[this._cadr], 32, 32, this.x - camera.x, this.y - camera.y, 32, 32);
	};
}

var Door = new DoorClass(6,6);
LayerCollision.setCollision(6,6,true);

Trigger.on("enter", function(value){
	if (value===0) Door.trigger();

});

var reg = new Region(10,10, 15,15,randomColor(0.4));
reg.initialization();
//console.log(reg);
Regions.newRegion(reg);







function update(){
	Grid.update();

	drawGrid();

	LayerCollision.update();

	Regions.update();

	Hero.update();

	Mouse.update();

	Create1.update();

	Selected.update();

	Orders.update();

	wallLayer.update();

	Layer.update();

	Door.update();
  		
}


function events(){
	if (isKeyDown("W") ){
		camera.translate(0,-10);
	}

	if (isKeyDown("S") ){
		camera.translate(0,10);
	}

	if (isKeyDown("A") ){
		camera.translate(-10,0);
	}

	if (isKeyDown("D") ){
		camera.translate(10,0);
	}

	if (isKeyDown("Q") ){
		camera.zoom(1);
	}

	if (isKeyDown("E") ){
		camera.zoom(-1);
	}

	if (isKeyDown("F") ){
		camera.center();
	}

}


///////////////////ИГРОВОЙ ЦИКЛ////////////////////
var gameEngineStart = function(callback){
	gameEngine = callback;
 	gameEngineStep();
};
 
var setGameEngine = function(callback){
	gameEngine = callback;
};

var gameEngineStep = function() {
	gameEngine();
	requestAnimationFrame(gameEngineStep);
};

var gameLoop = function(){
	//blank();
	background();
	update();
	Paths.update();
	events();
};
 
gameEngineStart(gameLoop);