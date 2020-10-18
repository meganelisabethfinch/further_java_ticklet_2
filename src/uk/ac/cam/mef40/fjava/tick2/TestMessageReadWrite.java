/*
 * Copyright 2020 Andrew Rice <acr31@cam.ac.uk>, Alastair Beresford <arb33@cam.ac.uk>, M.E. Finch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.cam.mef40.fjava.tick2;
// TODO: import required classes

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

class TestMessageReadWrite {

  static boolean writeMessage(String message, String filename) {
    // TODO: Create an instance of "TestMessage" with "text" set
    //      to "message" and serialise it into a file called "filename".
    //      Return "true" if write was successful; "false" otherwise.

    var testMessage = new TestMessage();
    testMessage.setMessage(message);

    try {
      FileOutputStream fos = new FileOutputStream(filename);
      ObjectOutputStream out = new ObjectOutputStream(fos);
      out.writeObject(testMessage);
    } catch (IOException e) {
      return false;
    }

    return true;
  }

  static String readMessage(String location) {
    // TODO: If "location" begins with "http://" or "https://" then
    // attempt to download and deserialise an instance of
    // TestMessage; you should use the java.net.URL and
    // java.net.URLConnection classes.  If "location" does not
    // begin with "http://" or "https://" attempt to deserialise
    // an instance of TestMessage by assuming that "location" is
    // the name of a file in the filesystem.
    //
    // If deserialisation is successful, return a reference to the
    // field "text" in the deserialised object. In case of error,
    // return "null".
    return null;
  }

  public static void main(String args[]) {
    // TODO: Implement suitable code to help you test your implementation
    //      of "readMessage" and "writeMessage".
  }
}
