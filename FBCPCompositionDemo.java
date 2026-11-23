import java.util.*;

/**
 * Effective Java 項目18「継承よりもコンポジションを選ぶ」のサンプル
 * 良い例：Set を継承せずに「持つ（has-a）」ことで、親クラスの内部実装に依存せずに機能を追加する。
 *
 * 題材：Set に「これまでに追加しようとした要素の延べ数」を数える機能を足したい。
 *
 * 【コンポジションの何がよいのか】
 *  ・中身の Set の実装の詳細（addAll が add を呼ぶか等）に影響されない。
 *    中身が内部で自分の add を何回呼んでも、ラッパーの add には戻ってこない。
 *  ・Set インタフェースの公開された契約にだけ依存するので、将来の JDK で
 *    中身の実装が変わっても壊れにくい。
 *  ・Set インタフェースに沿って転送しているので、中身の実装に新しいメソッドが
 *    増えても、数え漏れの抜け穴にはならない。
 *  ・コンストラクタで中身を受け取るので、HashSet でも TreeSet でも包める。
 *    Set の種類ごとにクラスを作る必要がない。
 */
public class FBCPCompositionDemo {

    // =====================================================================
    // 動作確認
    // =====================================================================
    public static void main(String[] args) {
        List<String> words = List.of("Snap", "Crackle", "Pop");

        InstrumentedSet<String> s = new InstrumentedSet<>(new HashSet<>());
        s.addAll(words);

        // 3 件追加して、正しく 3 と表示される
        System.out.println("コンポジション版 addCount = " + s.getAddCount() + "  （期待値 3）");

        // 中身を TreeSet に差し替えても、同じクラスでそのまま使える
        InstrumentedSet<String> t = new InstrumentedSet<>(new TreeSet<>());
        t.addAll(words);

        // 3 件追加して 3、中身は TreeSet なのでソートされて表示される
        System.out.println("TreeSet 版 addCount = " + t.getAddCount() + "  （期待値 3）, 中身 = " + t);
    }
}

/**
 * 転送クラス：Set をフィールドとして持ち、すべてのメソッドを中身にそのまま転送する。
 * 自分では何の機能も足さない、再利用のための部品。
 */
class ForwardingSet<E> implements Set<E> {
    // 中身の Set（HashSet でも TreeSet でもよい）
    private final Set<E> s;

    public ForwardingSet(Set<E> s) {
        this.s = s;
    }

    // ---- 以下はすべて中身の Set への単純な転送 ----
    @Override public int size()                                  { return s.size(); }
    @Override public boolean isEmpty()                           { return s.isEmpty(); }
    @Override public boolean contains(Object o)                  { return s.contains(o); }
    @Override public Iterator<E> iterator()                      { return s.iterator(); }
    @Override public Object[] toArray()                          { return s.toArray(); }
    @Override public <T> T[] toArray(T[] a)                      { return s.toArray(a); }
    @Override public boolean add(E e)                            { return s.add(e); }
    @Override public boolean remove(Object o)                    { return s.remove(o); }
    @Override public boolean containsAll(Collection<?> c)        { return s.containsAll(c); }
    @Override public boolean addAll(Collection<? extends E> c)   { return s.addAll(c); }
    @Override public boolean retainAll(Collection<?> c)          { return s.retainAll(c); }
    @Override public boolean removeAll(Collection<?> c)          { return s.removeAll(c); }
    @Override public void clear()                                { s.clear(); }
    @Override public boolean equals(Object o)                    { return s.equals(o); }
    @Override public int hashCode()                              { return s.hashCode(); }
    @Override public String toString()                           { return s.toString(); }
}

/**
 * ForwardingSet を継承し、add / addAll をオーバーライドして件数を数える（ラッパークラス）。
 * 継承版と同じオーバーライドだが、こちらは addAll の件数が正しく数えられる。
 */
class InstrumentedSet<E> extends ForwardingSet<E> {
    // 追加しようとした要素の延べ数
    private int addCount = 0;

    public InstrumentedSet(Set<E> s) {
        super(s);
    }

    @Override
    public boolean add(E e) {
        addCount++;               // 1 件追加
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();     // まとめて件数を加算
        // ↓ 継承版との違い：
        //   super.addAll は ForwardingSet#addAll、つまり「中身の Set の addAll」を呼ぶだけ。
        //   中身（HashSet）が内部で 1 件ずつ add() を呼んでも、それは中身自身の add であり、
        //   InstrumentedSet#add には戻ってこない。そのため件数が二重に加算されない。
        return super.addAll(c);
    }

    public int getAddCount() {
        return addCount;
    }
}
