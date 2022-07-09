#include "Arduino.h"
#include "keyword.h"

/* intr1 */
void toggleUW(){
	delayMicroseconds(2000); /* Chattering */
	if(digitalRead(intr1)!=LOW) return;
	extern bool togUW;
	togUW = !togUW;
}
/* intr2 */
void takePicture(){
	delayMicroseconds(2000);
	if(digitalRead(intr2)!=LOW) return;
	digitalWrite(ledR,255);
	digitalWrite(ledG,0);
	digitalWrite(ledB,0);
}
/* intr3 */
void takeOCR(){
	delayMicroseconds(2000);
	if(digitalRead(intr3)!=LOW) return;
	digitalWrite(ledR,255);
	digitalWrite(ledG,228);
	digitalWrite(ledB,0);
}
/* intr4 */
void findWay(){
	delayMicroseconds(2000);
	if(digitalRead(intr4)!=LOW) return;
	digitalWrite(ledR,0);
	digitalWrite(ledG,255);
	digitalWrite(ledB,0);
}
/* intr5 */
void toggleLED(){
	delayMicroseconds(2000);
	if(digitalRead(intr5)!=LOW) return;
	extern bool togLED;
	if(togLED == true){
		digitalWrite(ledR, 0);
		digitalWrite(ledG, 0);
		digitalWrite(ledB, 0);
	}
	togLED = !togLED;
}
/* vibration module */
void vibration(bool flag){
	if(flag){
		analogWrite(vibr, 64);
	}
	else{
		analogWrite(vibr,0);
	}
}
/* active buzzer */
void alarm(bool flag){
	if(flag){
		digitalWrite(buzz, HIGH);
	}
	else{
		digitalWrite(buzz, LOW);
	}
}
/* Servo */
void turnServo(Servo myServo, int dir){
	dir *= 90;
	myServo.write(dir);
}
/* Bluetooth */
void callBLE(char ch){
	extern Servo myServo;
	if(ch == 'a'){
      takePicture();
    }
    else if(ch == 'b'){
      takeOCR();
    }
    else if(ch == 'c'){
      findWay();
    }
    else if(ch == 'd'){
      toggleLED();
    }
	else if(ch == 'e'){
		turnServo(myServo, 2);
	}
	else if(ch == 'f'){
		turnServo(myServo, 0);
	}
	else if(ch == 'g'){
		turnServo(myServo, 1);
	}
	else if(ch == 'h'){
		toggleUW();
	}
}