// cribbage
// Jerred Shepherd

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class cribbage {

    private static final int NUMBER_OF_POINTS_PER_COMBINATION_EQUAL_TO_FIFTEEN = 2;
    private static final int NUMBER_OF_POINTS_FOR_ALL_CARDS_SAME_SUIT = 5;
    private static final int NUMBER_OF_POINTS_FOR_HAND_SAME_SUIT = 4;
    private static final int NUMBER_OF_POINTS_PER_PAIR = 2;

    public static void main(String[] args) throws FileNotFoundException {
        LinkedList<Input> inputs = getInput();
        LinkedList<Solution> solutions = solveInputs(inputs);
//        printSolutions(solutions);
    }

    private static LinkedList<Input> getInput() throws FileNotFoundException {
        File file = new File("cribbage.in");
        Scanner scanner = new Scanner(file);

        LinkedList<Input> inputs = new LinkedList<>();

        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            if (line.contains("0")) {
                break;
            }
            inputs.add(stringToInput(line));
        }

        return inputs;
    }

    private static void printSolutions(LinkedList<Solution> solutions) throws FileNotFoundException {
        File file = new File("cribbage.out");
        PrintWriter printWriter = new PrintWriter(file);

        solutions.forEach(solution -> {
            System.out.println(solution.points);
            printWriter.println(solution.points);
        });

        printWriter.close();
    }

    private static LinkedList<Solution> solveInputs(LinkedList<Input> inputs) {
        LinkedList<Solution> solutions = new LinkedList<>();
        inputs.forEach(input -> solutions.add(solveInput(input)));
        return solutions;
    }

    private static Solution solveInput(Input input) {
        int points;

        int pointsForPairs = calculatePointsForPairs(input);
        int pointsForFlushes = calculatePointsForFlushes(input);
        int pointsForRuns = calculatePointsForRuns(input);
        int pointsForFifteens = calculatePointsForFifteens(input);
        int pointsForNobs = calculatePointsForNobs(input);

        points = pointsForPairs + pointsForFlushes + pointsForRuns + pointsForFifteens + pointsForNobs;

        System.out.println(String.format("Cards\n%s\n\nPairs: %s\nFlushes: %s\nRuns: %s\nFifteens: %s\nNobs: %s\nTotal: %s\n\n",
                String.join("\n", input.cards.stream().map(Card::toString).collect(Collectors.toList())),
                pointsForPairs,
                pointsForFlushes,
                pointsForRuns,
                pointsForFifteens,
                pointsForNobs,
                points));

        return new Solution(points);
    }

    private static int calculatePointsForNobs(Input input) {
        List<Card> hand = input.cards;
        Card starter = input.starter;

        for (Card card : hand) {
            if (card.value == Card.Value.JACK && card.suit == starter.suit) {
                return 1;
            }
        }

        return 0;
    }

    private static int calculatePointsForFifteens(Input input) {
        Set<Pair> pairs = input.pairs;

//        System.out.println(pairs);

        return Math.toIntExact(pairs.stream().filter(pair -> pair.pairValue() == 15).count()) * NUMBER_OF_POINTS_PER_COMBINATION_EQUAL_TO_FIFTEEN;
    }

    private static int calculatePointsForRuns(Input input) {
        LinkedList<Card> cards = input.cards;
        
        Node head = new Node(null, null);

        int childValue = -1;
        List<Node> parents = new ArrayList<>();
        parents.add(head);

        for (Card card : cards) {
            Node firstParent = parents.get(0);

            Node newNode = new Node(card, parents);
            int cardValue = card.getValueAsInt();

            if (childValue == -1) {
                // first run
                parents.forEach(parent -> parent.children.add(newNode));
                childValue = card.getValueAsInt();
            } else if (cardValue == childValue) {
                // sibling
                parents.forEach(parent -> parent.children.add(newNode));
            } else {
                // child on new level
                parents = firstParent.children;
                parents.forEach(parent -> parent.children.add(newNode));
                childValue = card.getValueAsInt();
            }
        }

        System.out.println(head);

        return 0;
    }

    private static int calculatePointsForFlushes(Input input) {
        List<Card> hand = input.hand;
        Card starter = input.starter;

        Card.Suit suit = null;
        for (Card card : hand) {
            if (suit == null) {
                suit = card.suit;
            } else {
                if (card.suit != suit) {
                    return 0;
                }
            }
        }

        if (suit == starter.suit) {
            return NUMBER_OF_POINTS_FOR_ALL_CARDS_SAME_SUIT;
        } else {
            return NUMBER_OF_POINTS_FOR_HAND_SAME_SUIT;
        }
    }

    private static int calculatePointsForPairs(Input input) {
        Set<Pair> pairs = input.pairs;

        int numberOfPairs = Math.toIntExact(pairs.stream().filter(Pair::isPairSameValue).count());

        return numberOfPairs * NUMBER_OF_POINTS_PER_PAIR;
    }

    private static Input stringToInput(String string) {
        LinkedList<Card> cards = Stream.of(string)
                .map(s -> s.split(" "))
                .flatMap(Arrays::stream)
                .map(cribbage::stringToCard)
                .collect(Collectors.toCollection(LinkedList::new));
        return new cribbage.Input(cards);
    }

    private static Card stringToCard(String string) {
        char[] chars = string.toCharArray();
        Card.Value value = charToValue(chars[0]);
        Card.Suit suit = charToSuit(chars[1]);
        return new Card(value, suit);
    }

    private static Card.Suit charToSuit(char c) {
        switch (c) {
            case 'C':
                return Card.Suit.CLUB;
            case 'D':
                return Card.Suit.DIAMOND;
            case 'H':
                return Card.Suit.HEART;
            case 'S':
                return Card.Suit.SPADE;
            default:
                throw new IllegalArgumentException(String.format("Cannot convert %s to a suit", c));
        }
    }

    private static Card.Value charToValue(char c) {
        switch (c) {
            case 'A':
                return Card.Value.ACE;
            case '2':
                return Card.Value.TWO;
            case '3':
                return Card.Value.THREE;
            case '4':
                return Card.Value.FOUR;
            case '5':
                return Card.Value.FIVE;
            case '6':
                return Card.Value.SIX;
            case '7':
                return Card.Value.SEVEN;
            case '8':
                return Card.Value.EIGHT;
            case '9':
                return Card.Value.NINE;
            case 'T':
                return Card.Value.TEN;
            case 'J':
                return Card.Value.JACK;
            case 'Q':
                return Card.Value.QUEEN;
            case 'K':
                return Card.Value.KING;
            default:
                throw new IllegalArgumentException(String.format("Cannot convert %s to a card", c));
        }
    }

    private static class Node {
        Card card;
        List<Node> parent;
        List<Node> children;

        public Node(Card card, List<Node> parent) {
            this.card = card;
            this.parent = null;
            this.children = new ArrayList<>();
        }

        @Override
        public String toString() {
            return "Node{" +
                    "card=" + card +
                    ", children=" + children +
                    '}';
        }
    }

    private static class Input {
        LinkedList<Card> cards;
        List<Card> hand;
        Card starter;
        Set<Pair> pairs;

        public Input(LinkedList<Card> cards) {
            this.cards = cards;
            this.hand = new ArrayList<>(cards).subList(0, cards.size() - 1);
            this.starter = cards.getLast();
            cards.sort(Comparator.comparingInt(Card::getValueAsInt));

            pairs = new HashSet<>();
            for (int inner = 0; inner < cards.size(); inner++) {
                for (int outer = 0; outer < cards.size(); outer++) {
                    if (inner == outer || inner < outer) {
                        continue;
                    }
                    pairs.add(new Pair(cards.get(inner), cards.get(outer)));
                }
            }
        }

        @Override
        public String toString() {
            return "Input{" +
                    "cards=" + cards +
                    '}';
        }
    }

    private static class Solution {
        int points;

        public Solution(int points) {
            this.points = points;
        }
    }

    private static class Pair {
        Card cardOne;
        Card cardTwo;

        public Pair(Card cardOne, Card cardTwo) {
            this.cardOne = cardOne;
            this.cardTwo = cardTwo;
        }

        public int pairValue() {
            return cardOne.getValueAsInt() + cardTwo.getValueAsInt();
        }

        public boolean isPairSameValue() {
            return cardOne.value == cardTwo.value;
        }

        public boolean contains(Card card) {
            return (cardOne.equals(card) || cardTwo.equals(card));
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "cardOne=" + cardOne +
                    ", cardTwo=" + cardTwo +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return contains(pair.cardOne) && contains(pair.cardTwo);
        }

        @Override
        public int hashCode() {
            return cardOne.suit.ordinal() + cardTwo.suit.ordinal() + cardOne.value.ordinal() + cardTwo.suit.ordinal();
        }
    }

    private static class Card {
        Value value;
        Suit suit;

        Card(Value value, Suit suit) {
            this.value = value;
            this.suit = suit;
        }

        public int getValueAsInt() {
            switch (value) {
                case ACE:
                    return 1;
                case TWO:
                    return 2;
                case THREE:
                    return 3;
                case FOUR:
                    return 4;
                case FIVE:
                    return 5;
                case SIX:
                    return 6;
                case SEVEN:
                    return 7;
                case EIGHT:
                    return 8;
                case NINE:
                    return 9;
                case TEN:
                    return 10;
                case JACK:
                    return 10;
                case QUEEN:
                    return 10;
                case KING:
                    return 10;
                default:
                    throw new IllegalArgumentException(String.format("Cannot get the int card of %s", value));
            }
        }

        enum Suit {
            CLUB, DIAMOND, HEART, SPADE
        }

        enum Value {
            ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING
        }

        @Override
        public String toString() {
            return String.format("v=%s s=%s", value, suit);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Card card = (Card) o;
            return value == card.value &&
                    suit == card.suit;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, suit);
        }
    }
}
