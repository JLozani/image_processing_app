public class CustomQueue<E extends Object> {
   private CustomQueue<E> next;
   
   private E object;
   
   public CustomQueue() {
      next = null;
      object = null;
   }
   
   private CustomQueue(E _object) {
      next = null;
      object = _object;
   }
   
   public void add(E _object) {
      if(next == null && object == null) {
         object = _object;
      } else {
         CustomQueue<E> temp = this;
         
         while(temp.getNext() != null) {
            temp = temp.getNext();
         }
         
         temp.setNext(new CustomQueue<E>(_object));
      }
   }
   
   public E get() {
      if(next == null) {
         E temp = object;
         
         object = null;
         
         return temp;
      }
      
      E temp = object;
      
      object = next.getObject();
      next = next.getNext();
      
      return temp;
   }
   
   public boolean isEmpty() {
      if(next == null && object == null) {
         return true;
      }
      
      return false;
   }
   
   public int size() {
      if(next == null && object == null) {
         return 0;
      }
      
      int size = 1;
      CustomQueue<E> temp = this;
      
      while(temp.getNext() != null) {
         size++;
         temp = temp.getNext();
      }  
      
      return size;
   }
   
   private E getObject() {
      return object;
   }
   
   private CustomQueue<E> getNext() {
      return next;
   }
   
   private void setNext(CustomQueue<E> _next) {
      next = _next;
   }
}