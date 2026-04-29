# My Notes

## -- PHASE 0 --
* Server module: program that handles network requests to create and play games, stores games persistently
in a database and sends out notifications to all the players of a game
* Client module: command line program that players use to create and play a game of chess, client communicates
with server over the network to play a game with other clients
* Shared module: code library that contains rules and representation of a chess game that both client and server
use to exercise and validate game play

**Client and Server modules use Websocket to communicate with each other

### Method Overriding
* a subclass replaces an inherited method by redefining it
  * arg list must be the same
  * return type must be the same (or a subclass)
  * must not make access modifier more restrictive
  * must not throw new or broader checked exceptions
* can call the overridden version of the method by using super
  * ex: `Person.java, Employee.java (see toString() methods)`
* use @Override annotation to prevent typos
  * ex: `in previous ex, replace toString with tostring (lowercase 's') and see what happens`
    * remove @Override and replace toString with tostring

### Implementing a hashCode() Method
- if invoked on the same object more than once, the hashCode method must consistently return the same integer
- if two objects are equal using .equal(), then calling the hashCode method must produce the same integer result
- if not equal, producing distinct integer results for unequal objects may improve the performance of hash tables
- Ex: 
  - `public int hashCode() {
        int hash = 7; 
        hash = 31 * hash + (int) id; // use a prime number \n
        hash = 31 * hash + (name == null ? 0 : name.hashCode());
        hash = 31 * + (email == null ? 0 : email.hashCode());
        return hash;
  }`

### Method Overloading
- reuse a method name with a different arg list
- use when you need to reuse a method name for things that are conceptually the same thing, but the code would be 
different because of different data types

### Hash Tables
- watch video if you wanna use hash tables to store pieces and their positions

GENERAL NOTES:
- hashCode() method: basically presents a default solution for something. use @Override to get a different solution
- toString() method: returns a string representation of the object (override to make useful)
- equals(Object obj) method: indicates whether some other object is "equal to" this one (same data, but not necessarily
the same exact object)
  - compares addresses
    - if you're checking addresses, you don't need to override 
- getClass() method: returns runtime class of this Object
- every class inherits from a parent
- .equals() is the same as ==
  - ex: `if (obj.getClass() != this.getClass()) { return false; } is the same as
if (obj.getClass().equals(this.getClass()) { return false; }`
  - 