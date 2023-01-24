package com.example.democloudstreamwebfux.intergration.directChannel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.democloudstreamwebfux.intergration.directChannel.Book.Genre;

@Component
public  class BookPublisher {

    private long nextBookId ;

    public BookPublisher() {
        this.nextBookId = 1001L;
    }

    public List<Book> getBooks() {

        List<Book> books = new ArrayList<>() ;
        books.add(createFantasyBook());
        books.add(createFantasyBook());
        books.add(createFantasyBook());
        books.add(createFantasyBook());
        books.add(createHorrorBook());
        books.add(createHorrorBook());
        books.add(createHorrorBook());
        books.add(createHorrorBook());
        books.add(createHorrorBook());
        books.add(createRomanceBook());
        books.add(createRomanceBook());
        books.add(createRomanceBook());
        books.add(createRomanceBook());
        books.add(createRomanceBook());
        books.add(createThrillerBook());
        books.add(createThrillerBook());
        books.add(createThrillerBook());
        books.add(createThrillerBook());
        books.add(createThrillerBook());
        
        return books ;
        
    }

    Book createFantasyBook() {
        return createBook("",Genre.fantasy) ;
    }

    Book createHorrorBook() {
        return createBook("", Genre.horror);
    }

    Book createRomanceBook() {

        return createBook("", Genre.romance);
    }

    Book createThrillerBook() {

        return createBook("", Genre.thriller);
    }

    Book createBook(String title, Genre gener) {
        Book book = new Book();
        book.setBookId(nextBookId++);
        if (title == "") {
          title = "# " + Long.toString(book.getBookId());
        }
        book.setGenre(gener);
        return book;
    }
}
