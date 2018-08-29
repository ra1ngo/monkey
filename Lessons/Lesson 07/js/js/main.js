

var monk = new Monkey();
var mainScreen = monk.createScreen();
monk.setScreen(mainScreen);

var texture1 = monk.createTexture("./img/elli_walk.png", 1.1, 0.5);
var texture2 = monk.createTexture("./img/img.jpg", 1.0, 1.0);
//console.log(monk);











function loop(){
    // очищаем canvas
    monk.clear();

    texture1.draw();
    texture2.draw();
    //texture1.draw();


    requestAnimationFrame(loop);
};

loop();