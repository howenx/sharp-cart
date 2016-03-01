package test;

import akka.actor.UntypedActor;
import play.Logger;

public class CalculatorActor1 extends UntypedActor {
  @Override
  public void onReceive(Object message) {

    Logger.info("======CalActor1=========");

  }
}
