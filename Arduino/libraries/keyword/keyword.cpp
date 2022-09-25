#include "Arduino.h"
#include "keyword.h"

/* intr1 */
void toggleSonar(){
	delayMicroseconds(3000); /* Chattering */
	if(digitalRead(intr1)!=LOW) return;
	extern bool togSonar;
	togSonar = !togSonar;
	Serial.println(5);
}
/* intr2 */
void takePicture(){
	delayMicroseconds(3000);
	if(digitalRead(intr2)!=LOW) return;
	digitalWrite(ledR,255);
	digitalWrite(ledG,0);
	digitalWrite(ledB,0);
	Serial2.println(1);
}
/* intr3 */
void takeOCR(){
	delayMicroseconds(3000);
	if(digitalRead(intr3)!=LOW) return;
	digitalWrite(ledR,255);
	digitalWrite(ledG,228);
	digitalWrite(ledB,0);
	Serial2.println(2);
}
/* intr4 */
void findWay(){
	delayMicroseconds(3000);
	if(digitalRead(intr4)!=LOW) return;
	digitalWrite(ledR,0);
	digitalWrite(ledG,255);
	digitalWrite(ledB,0);
	Serial2.println(3);
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
	Serial2.println(4);
}
/* vibration module */
void vibration(bool flag){
	if(flag){
		analogWrite(vibr, 128);
		Serial.println("vibra on");
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
/* Servo
	dir == 0   : ?
	dir == 90  : ?
	dir == 180 : ?
 */
void turnServo(Servo servo, int dir){
	dir *= 90;
	servo.write(dir);
}
void switchServo(Servo servo){
	extern bool sDir[3];
	// 0 : left
	// 1 : mid
	// 2 : right
	if(!sDir[0] and !sDir[1] and !sDir[2]){ 	// 000
		turnServo(servo, 1);
	}
	else if(!sDir[0] and !sDir[1] and sDir[2]){ // 001
		turnServo(servo, 0);
	}
	else if(!sDir[0] and sDir[1] and !sDir[2]){ // 010
		turnServo(servo, 2);
	}
	else if(!sDir[0]){ 							// 011
		turnServo(servo, 0);
	}
	else if(!sDir[1] and !sDir[2]){				// 100
		turnServo(servo, 2);
	}
	else if(!sDir[1]){ 							// 101
		turnServo(servo, 1);
	}
	else if(!sDir[2]){ 							// 110
		turnServo(servo, 2);
	}
	else{ 										// 111
		turnServo(servo, 1);
	}
}
/* Bluetooth */
void callBLE(char ch){
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
		toggleSonar();
	}
}