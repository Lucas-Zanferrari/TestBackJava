package microservice.models;

import java.util.Date;
import javax.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "spends")
public class Spend {
    @Id
    private String _id;
    
    @NotNull
    private String description;
    
    @NotNull
    private BigDecimal value;

    @NotNull
    private String userCode;

    private String category;

    @NotNull
    @JsonFormat(shape=JsonFormat.Shape.STRING, 
                pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", 
                timezone="UTC")
    private Date date;

    public Spend() { }

    public Spend(Spend otherSpend) { 
        this.description = otherSpend.getDescription();
        this.value = otherSpend.getValue();
        this.userCode = otherSpend.getUserCode();
        this.category = otherSpend.getCategory();
        this.date = otherSpend.getDate();
    }

    public Spend(String _id, 
                 String description, 
                 BigDecimal value, 
                 String userCode, 
                 String category, 
                 Date date) {
        this._id = _id;
        this.description = description;
        this.value = value;
        this.userCode = userCode;
        this.category = category;
        this.date = date;
    }

    public String get_id() { return _id; }

    public String getDescription() { return description; }

    public BigDecimal getValue() { return value; }

    public String getUserCode() { return userCode; }

    public String getCategory() { return category; }

    public Date getDate() { return date; }

    public void set_id(String _id) { this._id = _id; }

    public void setDescription(String description) { this.description = description; }

    public void setValue(BigDecimal value) { this.value = value.setScale(2, BigDecimal.ROUND_HALF_UP); }

    public void setUserCode(String userCode) { this.userCode = userCode; }

    public void setCategory(String category) { this.category = category; }

    public void setDate(Date date) { this.date = date; }

}