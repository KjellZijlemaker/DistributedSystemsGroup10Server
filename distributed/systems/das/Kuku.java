package distributed.systems.das;

import java.io.Serializable;

class Kuku implements Serializable {
       private static final long serialVersionUID = 1L;

        public String a = null;
        public String b = null;


        public String getA(){
            return this.a;
        }

        public String getB(){
            return this.b;
        }

        public void setA(String a){
            this.a = a;
        }
}
