class BookController {
  
  def scaffold = Book;
/*
  def index = { redirect(action:list,params:params) }

  def list = {
        if(!params.max) params.max = 10
        [ bookList: Book.list( params ) ]
  }

                
  def save = {
    def book = new Book(params)
    if(!book.hasErrors() && book.save()) {
      flash.message = "Book ${book.title} created"
      redirect(action:show,id:book.title)
    }
    else {
      render(view:'create',model:[book:book])
    }
  }
*/

}
