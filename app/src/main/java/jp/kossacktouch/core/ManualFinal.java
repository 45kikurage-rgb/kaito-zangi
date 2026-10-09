package jp.kossacktouch.core;
/** Q5 hands control back to the user. No text-entry or submission operation is representable. */
public final class ManualFinal {
 public enum Step { WAIT,OPEN_KEYBOARD,FOCUS_INPUT,DISPLAY,MANUAL_OPEN,TIMEOUT }
 private final ElementMonitor keyboard=new ElementMonitor();private long requestedAt=-1;private Step terminal;private boolean engaged;
 public Step observe(String questionKey,boolean stable,boolean keyboardVisible,boolean opener,int inputs,boolean clickableInput,long now){
  if(terminal!=null)return terminal;
  if(stable)engaged=true;
  boolean confirmed=keyboard.observe(questionKey+":"+stable+":"+keyboardVisible,now);
  if(stable&&keyboardVisible&&confirmed)return terminal=Step.DISPLAY;
  if(requestedAt>=0&&now-requestedAt>=10000)return terminal=Step.TIMEOUT;
  if(!stable||keyboardVisible)return Step.WAIT;
  if(requestedAt>=0)return Step.WAIT;
  if(opener){requestedAt=now;return Step.OPEN_KEYBOARD;}
  if(inputs==1&&clickableInput){requestedAt=now;return Step.FOCUS_INPUT;}
  return terminal=Step.MANUAL_OPEN;
 }
 public boolean started(){return engaged;}
 public void reset(){requestedAt=-1;terminal=null;engaged=false;keyboard.reset();}
}
