
(define a 456)
(define b 21)
(define x 20)
(define c x)
(define d 101)
(define f (lambda (x y) (+ (if (or (= x 3 ) (= x 4)) 100 200) (let ((k 20)) (+ y (+ a k))))))
(f (+ 1 2) (if (and (= c 20) (= b 21) (= d 100)) 50 10))
