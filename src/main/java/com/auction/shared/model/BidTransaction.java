import java.math.BigDecimal;
import java.time.LocalDateTime;
//BidTransaction: lịch sử giao dịch
public class BidTransaction extends Entity {
    private int auctionId;
    private int bidderId;
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;

    public BidTransaction(int id, int auctionId, int bidderId, BigDecimal bidAmount, LocalDateTime bidTime) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidTime = bidTime;
    }

    //tạo mới một lượt đặt giá
    public BidTransaction(int auctionId, int bidderId, BigDecimal bidAmount) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.bidAmount = bidAmount;
        this.bidTime = LocalDateTime.now();
    }

    // Getter và Setter
    public int getAuctionId() { return auctionId; }
    public int getBidderId() { return bidderId; }
    public BigDecimal getBidAmount() { return bidAmount; }
    public LocalDateTime getBidTime() { return bidTime; }

    @Override
    public String toString() {
        return String.format("Bid[Auction: %d, User: %d, Amount: %s, Time: %s]",
                auctionId, bidderId, bidAmount, bidTime);
    }
}