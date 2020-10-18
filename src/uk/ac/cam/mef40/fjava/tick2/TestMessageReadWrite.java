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

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

class TestMessageReadWrite {

  static boolean writeMessage(String message, String filename) {
    // Create an instance of "TestMessage" with "text" set
    //      to "message" and serialise it into a file called "filename".
    //      Return "true" if write was successful; "false" otherwise.

    var testMessage = new TestMessage();
    testMessage.setMessage(message);

    try {
      FileOutputStream fos = new FileOutputStream(filename);
      ObjectOutputStream out = new ObjectOutputStream(fos);
      out.writeObject(testMessage);
      out.close();
    } catch (IOException e) {
      return false;
    }

    return true;
  }

  static String readMessage(String location) {
    // If "location" begins with "http://" or "https://" then
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

    ObjectInputStream ois;

    try {
      if (location.startsWith("http://") || location.startsWith("https://")) {
        // Download and deserialise a TestMessage
        URL url = new URL(location);
        URLConnection connection = url.openConnection();
        InputStream stream = connection.getInputStream();
        ois = new ObjectInputStream(stream);
      } else {
        // Assume location is name of a file in the filesystem
        FileInputStream fis = new FileInputStream(location);
        ois = new ObjectInputStream(fis);
      }

      TestMessage obj = (TestMessage) ois.readObject();
      return obj.getMessage();
    } catch (IOException | ClassNotFoundException e) {
      System.err.println(e.getMessage());
      return null;
    }
  }

  public static void main(String args[]) {
    try {
      String location = args[0];
      String msg = readMessage(location);
      System.out.println(msg);
    } catch (ArrayIndexOutOfBoundsException e) {
      System.err.println("This program takes one argument: <location>");
    }
  }
}
