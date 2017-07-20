package notafiscal;

public class as {

	public static int blackjack(int first, int second){
		if(first > 21 && second > 21)
			return 0;
		if(first == second)
			return first;
		if(first == 21 || second == 21)
			return 21;	
		
		if( first > second ){
			if( first > 21 && second < first)
				return second;
			else
				return first;
		} else {
			if( second > 21 && first < second)
				return first;
			else
				return second;
		}		
	}
	
	public static void main(String[] args) {
		System.out.println(blackjack(10, 20));
		System.out.println(blackjack(10, 21));
		System.out.println(blackjack(20, 23));
		System.out.println(blackjack(23, 20));
		System.out.println(blackjack(20, 20));		
	}
}
