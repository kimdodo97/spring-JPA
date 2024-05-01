package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Test
    @DisplayName("상품 주문")
    void test1() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("JPA",10000, 10);
        int orderCount = 2;
        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(getOrder.getStatus(), OrderStatus.ORDER);
        assertEquals(1,getOrder.getOrderItems().size());
        assertEquals(10000*orderCount,getOrder.getTotalPrice());
        assertEquals(8,book.getStockQuantity());
    }

    @Test
    @DisplayName("주문 취소")
    void test2() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("JPA",10000, 10);
        int orderCount = 2;
        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);
        assertEquals(OrderStatus.CANCEL,getOrder.getStatus());
        assertEquals(10,book.getStockQuantity());
    }
    @Test
    @DisplayName("상품주문 재고수량 초과")
    void test3() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("JPA",10000, 10);
        int orderCount = 12;
        //expect
        Assertions.assertThrows(NotEnoughStockException.class,
                () -> orderService.order(member.getId(), book.getId(),orderCount));
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울","강변불록","123-123"));
        em.persist(member);
        return member;
    }
}