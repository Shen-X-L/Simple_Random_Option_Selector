package com.example.randomroulettewheel.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProbabilityArray implements Parcelable{
    public static class Probability implements Parcelable {
        public String optionName = "";//选项名字
        public double weight = 0;//权重
        public double probability = 0;//概率
        public boolean isLocked = false;//是否锁定概率 true锁定 false解锁
        // 全参数构造
        public Probability(String optionName, double weight, double probability, boolean isLock) {
            this.optionName = optionName;
            this.weight = weight;
            this.probability = probability;
            this.isLocked = isLock;
        }
        //空构造
        public Probability(){}

        //跨界面传输读取构造
        protected Probability(Parcel in) {
            optionName = in.readString();
            weight = in.readDouble();
            probability = in.readDouble();
            isLocked = in.readByte() != 0;
        }

        //反序列号时创建对象实例
        public static final Creator<Probability> CREATOR = new Creator<Probability>() {
            @Override
            public Probability createFromParcel(Parcel in) {
                return new Probability(in);
            }

            @Override
            public Probability[] newArray(int size) {
                return new Probability[size];
            }
        };
        @Override
        public int describeContents() {
            return 0;
        }
        //序列化写入dest
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(optionName);
            dest.writeDouble(weight);
            dest.writeDouble(probability);
            dest.writeByte((byte) (isLocked ? 1 : 0));
        }
        public String getOptionName() {
            return optionName;
        }
        public Probability setOptionName(String optionName) {
            this.optionName = optionName;
            return this;
        }
        public double getWeight(){
            return weight;
        }
        //设置权重
        public Probability setWeight(double weight){
            if(weight<0){
                throw new IllegalArgumentException("权重值不能为负数");
            }
            this.weight=weight;
            return this;
        }
        public double getProbability() {
            return probability;
        }
        //设置概率
        public Probability setProbability(double probability) {
            if (probability < 0 || probability > 1) {
                throw new IllegalArgumentException("概率值必须为0~1之间的非空值");
            }
            this.probability = probability;
            return this;
        }
        public boolean isLocked() {
            return isLocked;
        }
        public Probability setLock(boolean Lock) {
            isLocked = Lock;
            return this;
        }
        // 转为字符串 用于debug
        @NonNull
        @Override
        public String toString() {
            return "Probability{" +
                    "optionName='" + optionName + '\'' +
                    ", weight=" + weight +
                    ", probability=" + probability +
                    ", isLocked=" + isLocked +
                    '}';
        }
        // 转为Json格式
        public String toJson(){
            try{
                JSONObject json = new JSONObject();
                json.put("optionName", optionName);
                json.put("weight",weight);
                return json.toString();
            } catch (JSONException e) {
                return "{}";
            }
        }
        //从Json转为概率对象
        public static Probability fromJson(String jsonStr){
            Probability prob = new Probability();
            try {
                JSONObject json = new JSONObject(jsonStr);
                prob.optionName = json.optString("optionName", ""); // 提供默认值
                prob.weight = json.optDouble("weight", 0.0);
            } catch (JSONException e) {
                // 留空，返回带默认值的对象
            }
            return prob;
        }
    }

    public ArrayList<Probability> array;
    public double weightSum;//权重和
    public double unlockedProbabilitySum;//没有被锁定,可改变的概率的总和
    public int unlockerNumber;//无锁的对象个数

    protected ProbabilityArray(Parcel in) {
        array = in.createTypedArrayList(Probability.CREATOR);
        weightSum = in.readDouble();
        unlockedProbabilitySum = in.readDouble();
        unlockerNumber = in.readInt();
    }
    public static final Creator<ProbabilityArray> CREATOR = new Creator<ProbabilityArray>() {
        @Override
        public ProbabilityArray createFromParcel(Parcel in) {
            return new ProbabilityArray(in);
        }

        @Override
        public ProbabilityArray[] newArray(int size) {
            return new ProbabilityArray[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(array);
        dest.writeDouble(weightSum);
        dest.writeDouble(unlockedProbabilitySum);
        dest.writeInt(unlockerNumber);
    }

    //转为Json对象
    public String toJson(){
        JSONArray jsonArray = new JSONArray();
        for (Probability prob : array) {
            jsonArray.put(prob.toJson()); // 复用Probability的toJson()
        }
        return jsonArray.toString();
    }
    public static ProbabilityArray fromJson(String jsonStr){
        ProbabilityArray object = new ProbabilityArray();
        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i = 0; i < jsonArray.length(); i++) {
                object.array.add(Probability.fromJson(jsonArray.getString(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        object.calibrateProbabilitiesByWeights();
        return object;
    }

    //添加监听器接口
    private final List<DataChangeListener> listeners = new ArrayList<>();
    public interface DataChangeListener {
        void onDataChanged();
    }
    //添加监听器
    public void addDataChangeListener(DataChangeListener listener) {
        listeners.add(listener);
    }
    //通知所有监听器数据已变化
    public void notifyDataChanged() {
        for (DataChangeListener listener : listeners) {
            listener.onDataChanged();
        }
    }
    //移除监听
    public void removeDataChangeListener(DataChangeListener listener) {
        listeners.remove(listener);
    }
    //构造函数
    public ProbabilityArray(){
        array = new ArrayList<Probability>();
        weightSum = 0;
        unlockedProbabilitySum = 1;
        unlockerNumber = 0;
    }
    //检测是否在范围内
    private void checkBounds(int index) {
        if(index < 0 || index >= array.size()) {
            throw new IndexOutOfBoundsException("无效索引: " + index + "，有效范围: 0-" + (array.size()-1));
        }
    }
    private void checkBounds(Probability object) {
        for(int i = 0;i < array.size();++i){
            if(object == array.get(i)) return;
        }
            throw new IndexOutOfBoundsException("无效对象: " + object.toString() + "，有效范围: 0-" + (array.size()-1));
    }
    //设置名字
    public void setOptionName(int index,String optionName){
        checkBounds(index);
        array.get(index).setOptionName(optionName);
    }
    public void setOptionName(Probability object,String optionName){object.setOptionName(optionName);}
    //获取名字
    public String getOptionName(int index){
        checkBounds(index);
        return array.get(index).getOptionName();
    }
    public String getOptionName(Probability object){return object.getOptionName();}
    //设置是否加锁 并更改对应未锁对象个数和
    public void setLock(int index, Boolean lock){
        checkBounds(index);
        Probability that=array.get(index);
        if(that.isLocked() != lock){//改变锁结构
            if(lock){//上锁
                unlockerNumber--;//减少无锁数
                unlockedProbabilitySum -=that.getProbability();//减少无锁概率和
            }else{//解锁
                unlockerNumber++;//增加无锁数
                unlockedProbabilitySum +=that.getProbability();//增加无锁概率和
            }
            that.setLock(lock);
        }
    }
    public void setLock(Probability object,Boolean lock){
        setLock(getIndex(object),lock);
        return;
    }
    //该值是否已锁
    public boolean isLock(int index){
        checkBounds(index);
        return array.get(index).isLocked();
    }
    public boolean isLock(Probability object){return object.isLocked();}
    //设置概率并更新其他对象值
    public void setProbability(int index, double probability) {
        if(array.size() <= 1) return;
        //我才想到可以将未锁值加锁后同已锁处理再解锁
        checkBounds(index);
        Probability that = array.get(index);
        //储存之前锁
        boolean wasLocked=that.isLocked();
        //加锁
        setLock(index,true);
        //修改概率并储存返回码
        handleLockedProbabilityChange(that, probability);
        //回到之前状态
        setLock(index,wasLocked);

        //回调更新UI数据
        notifyDataChanged();

        return;
    }
    public void setProbability(Probability object, double probability) {
        if(array.size() <= 1) return;
        //储存之前锁
        boolean wasLocked=object.isLocked();
        //加锁
        setLock(object,true);
        //修改概率并储存返回码
        handleLockedProbabilityChange(object, probability);
        //回到之前状态
        setLock(object,wasLocked);

        //回调更新UI数据
        notifyDataChanged();

        return;
    }
    //设置已锁值概率并更新其他对象值的实际方法
    private void handleLockedProbabilityChange(Probability object, double newProbability) {
        double tempProbability = object.getProbability();
        if(newProbability == tempProbability) return;
        //改变已锁值的概率
        //超过概率最大值 (未锁概率和+原先值)
        if (newProbability >= unlockedProbabilitySum + tempProbability) {
            //使用全部未锁概率和
            newProbability = unlockedProbabilitySum + tempProbability;
            unlockedProbabilitySum = 0;
            //更新所有未锁值置空
            scaleUnlockedProbabilities(0);
            //更新新值
            object.setProbability(newProbability);
            object.setWeight(newProbability * weightSum);
            //返回0x010 表示已锁值取最大 不能再取值 锁住滑条向右移动
            return ;
        } else if (newProbability >= 0) {
            //缩放比例=新未锁概率和(原概率和-新值+原先值)/原概率和
            //所有未锁值按 缩放比例(新/旧) 放缩
            double scaleFactor = (unlockedProbabilitySum - newProbability + object.getProbability()) / unlockedProbabilitySum;
            //更新未锁概率和
            unlockedProbabilitySum = unlockedProbabilitySum - newProbability + object.getProbability();
            //其他未锁值是否为0
            boolean areUnlockedZero =true;
            for(Probability temp:array){
                if (!temp.isLocked() && temp.getProbability() > 0) {
                    areUnlockedZero = false;
                    break;
                }
            }
            //放缩非零未锁值
            if(!areUnlockedZero) scaleUnlockedProbabilities(scaleFactor);
            //所有未锁值为零或无未锁值
            else {
                calibrateUnlockedNumber();
                //存在为0的未锁值
                double unlockedProbability = unlockedProbabilitySum/unlockerNumber;
                for (Probability temp : array) {
                    if (!temp.isLocked()) {
                        temp.setProbability(unlockedProbability);
                        temp.setWeight(temp.getProbability() * weightSum);
                    }
                }
            }
            //更新新值
            object.setProbability(newProbability);
            object.setWeight(newProbability * weightSum);
            //返回0x100 表示能再取值
            return ;
        } else {
            throw new IllegalArgumentException("概率不能为负");
        }
    }
    //返回该值是否可修改 向左减少 向右增加
    //0b__ 0b_1是右方向解锁 0b_0是右方向锁定 0b1_是左方向解锁 0b0_是左方向锁定
    public int canItMove(Probability object){
        calibrateUnlockedNumber();
        calibrateUnlockedProbability();
        int canMoveBit = 0b00;
        //无未锁值 或本身是唯一未锁值
        if(unlockerNumber == 0 || (unlockerNumber == 1 && !object.isLocked))return canMoveBit;
        //可向左移动
        if(object.getProbability() > 0) canMoveBit += 0b10;
        //可向右移动
        if(object.getProbability() < maxProbability(object)) canMoveBit += 0b01;
        return canMoveBit;

    }
    //可改变的最大值
    public double maxProbability(int index){
        checkBounds(index);
        Probability object=array.get(index);
        if (object.isLocked()) return unlockedProbabilitySum + getProbability(getIndex(object));
        else return unlockedProbabilitySum;
    }
    public double maxProbability(Probability object){
        if (object.isLocked()) return unlockedProbabilitySum + getProbability(getIndex(object));
        else return unlockedProbabilitySum;
    }
    //获取index对应概率
    public double getProbability(int index){
        checkBounds(index);
        return array.get(index).getProbability();
    }
    public double getProbability(Probability object){return getProbability(getIndex(object));}
    //设置权重并更新其他对象值
    public void setWeight(int index, double weight){
        checkBounds(index);
        Probability that=array.get(index);
        if(weight<0){
            throw new IllegalArgumentException("权重值不能为负数");
        }
        else{
            boolean wasLocked=that.isLocked();
            //解锁 视为无锁对象修改权重 再上锁
            setLock(index,false);
            //无锁权重和
            double unlockedWeightSum = weightSum* unlockedProbabilitySum;
            //放缩系数
            double scaleFactor = 0;
            if(unlockedWeightSum <= 0){
                throw new IllegalArgumentException("未锁权重小于等于0");
            }
            //放缩比例=更新后权重(更新前-旧+新)/更新前权重
            scaleFactor=(unlockedWeightSum+weight-that.getWeight())/(unlockedWeightSum);
            //更新已锁值权重 保持概率
            scaleLockedWeights(scaleFactor);
            //更新本权重
            that.setWeight(weight);
            //更新权重和
            updateWeightSum();
            //更新未锁值概率
            updateProbabilitiesFromWeights();
            //回归原状态
            setLock(index,wasLocked);

            //回调更新UI数据
            notifyDataChanged();

        }
    }
    public void setWeight(Probability object, double weight){
        if(weight<0){
            throw new IllegalArgumentException("权重值不能为负数");
        }
        else{
            boolean wasLocked = object.isLocked();
            //解锁 视为无锁对象修改权重 再上锁
            setLock(object,false);
            //无锁权重和
            double unlockedWeightSum = weightSum * unlockedProbabilitySum;
            //放缩系数
            double scaleFactor = 0;
            if(unlockedWeightSum < 0){
                setLock(object,wasLocked);
                throw new IllegalArgumentException("未锁权重小于0");
            }else if(unlockedWeightSum == 0 && weight > 0) {
                setLock(object, wasLocked);
                throw new IllegalArgumentException("未锁权重等于0");
            }else if(unlockedWeightSum == 0 && weight == 0){
                setLock(object, wasLocked);
                return;
            }else{
                //放缩比例=更新后权重(更新前-旧+新)/更新前权重
                scaleFactor=(unlockedWeightSum + weight - object.getWeight()) / (unlockedWeightSum);
                //更新已锁值权重 保持概率
                scaleLockedWeights(scaleFactor);
            }
            //更新本权重
            object.setWeight(weight);
            //更新权重和
            updateWeightSum();
            //更新未锁值概率
            updateProbabilitiesFromWeights();
            //回归原状态
            setLock(object,wasLocked);

            //回调更新UI数据
            notifyDataChanged();

        }
    }
    //放缩已锁值权重 保持概率
    private void scaleLockedWeights(double scaleFactor) {
        for (Probability temp : array) {
            if (temp.isLocked()) {
                temp.setWeight(temp.getWeight() * scaleFactor);
            }
        }
    }
    //更新未锁值概率 基于权重
    private void updateProbabilitiesFromWeights() {
        for (Probability temp : array) {
            if (!temp.isLocked()) {
                temp.setProbability(temp.getWeight() / weightSum);
            }
        }
    }
    //放缩无锁值的 权重与概率
    private void scaleUnlockedProbabilities(double scaleFactor) {
        for (Probability temp : array) {
            if (!temp.isLocked()) {
                temp.setProbability(temp.getProbability() * scaleFactor);
                temp.setWeight(temp.getProbability() * weightSum);
            }
        }
    }
    //检测并更新未锁值个数
    public void calibrateUnlockedNumber(){
        int tempNumber = 0;
        for(Probability temp:array){
            if(!temp.isLocked) ++tempNumber;
        }
        if(tempNumber != unlockerNumber){
            int debugNumber = unlockerNumber;
            unlockerNumber = tempNumber;
            throw new IllegalStateException("未锁值总数不匹配 (预期: " + tempNumber + ", 实际: " + debugNumber + ")");
        }
    }
    //获取权重值
    public double getWeight(int index){
        checkBounds(index);
        return array.get(index).getWeight();
    }
    public double getWeight(Probability object){
        return object.getWeight();
    }
    //更新权重和
    public void updateWeightSum(){
        weightSum = 0;
        for(Probability temp:array){
            weightSum += temp.getWeight();
        }
    }
    //权重为零时,重置值
    public void WeightSumIsZero(){
        if(weightSum <= 0) {
            int objectNumber = array.size();
            weightSum =0 ;
            unlockedProbabilitySum = 0;
            if (unlockerNumber == 0){
                for(Probability temp : array){
                    temp.setProbability((double) 1 / objectNumber);
                    temp.setWeight(1);
                    ++weightSum;
                }
            }else{
                for(Probability temp : array){
                    if(!temp.isLocked()){
                        temp.setProbability((double) 1 / unlockerNumber);
                        temp.setWeight(1);
                        ++weightSum;
                        unlockedProbabilitySum += (double) 1 / unlockerNumber;
                    }
                }
            }
        }
    }
    //概率归一化
    public void normalizeProbabilities(){
        double probabilitySum = 0;
        for(Probability temp:array){
            probabilitySum += temp.getProbability();
        }
        for(Probability temp:array){
            temp.setProbability(temp.getProbability() / probabilitySum);
        }
    }
    //检测并更新未锁概率和
    public void calibrateUnlockedProbability(){
        unlockedProbabilitySum = 1;
        for(Probability temp:array){
            if(temp.isLocked()){
                unlockedProbabilitySum -= temp.getProbability();
            }
        }
        if(unlockedProbabilitySum < 0){
            calibrateProbabilitiesByWeights();
            throw new IllegalArgumentException("未锁概率和小于0");
        }
    }
    //基于权重更新概率
    public void calibrateProbabilitiesByWeights(){
        updateWeightSum();
        WeightSumIsZero();
        if(weightSum < 0){
            throw new IllegalArgumentException("总权重小于0");
        }else if(weightSum == 0){

        }
        for(Probability temp:array){
            temp.setProbability(temp.getWeight() / weightSum);
        }

        //回调更新UI数据
        notifyDataChanged();


    }
    //基于概率更新权重
    public void calibrateWeightsByProbabilities(){
        normalizeProbabilities();
        updateWeightSum();
        WeightSumIsZero();
        for(Probability temp:array){
            temp.setWeight(temp.getProbability() * weightSum);
        }

        //回调更新UI数据
        notifyDataChanged();


    }
    //对锁定值基于概率更新权重 对未锁值基于未锁值权重比重分配权重和概率
    public void calibratePartialLockedValues(){
        updateWeightSum();
        WeightSumIsZero();
        double oldUnlockedWeightSum = 0;
        unlockedProbabilitySum = 1;
        for(Probability temp:array){
            if(temp.isLocked()){
                temp.setWeight(temp.getProbability() * weightSum);
                //新未锁值概率和
                unlockedProbabilitySum -= temp.getProbability();
            }else{
                //旧未锁值权重和
                oldUnlockedWeightSum += temp.getWeight();
            }
        }if(oldUnlockedWeightSum != 0){
            double newUnlockedProbabilitySum = weightSum * unlockedProbabilitySum;
            double scaleFactor = newUnlockedProbabilitySum / oldUnlockedWeightSum;
            //放缩比例=新未锁值可分配权重(新权重和*新未锁概率和)/旧未锁值权重和
            for(Probability temp:array){
                if(!temp.isLocked()){
                    temp.setWeight(temp.getWeight() * scaleFactor);
                    temp.setProbability(temp.getWeight() / weightSum);
                }
            }
        }else{
            for(Probability temp:array){
                if(!temp.isLocked()){
                    temp.setProbability(unlockedProbabilitySum / unlockerNumber);
                    temp.setWeight(weightSum * temp.getProbability());
                }
            }
        }


        //回调更新UI数据
        notifyDataChanged();


    }
    //添加概率对象
    public void add(){
        for(Probability temp:array) {
            if (temp.isLocked()) {
                //存在已锁值 增加的对象不能有权重改变概率
                array.add(new Probability().setOptionName("选项" + array.size()));
                ++unlockerNumber;
                return;
            }
        }
        //不存在已锁值 增加的对象可以改变其他对象概率
        array.add(new Probability().setWeight(1).setOptionName("选项" + array.size()));
        ++unlockerNumber;
        calibratePartialLockedValues();
        return;
    }
    public void add(Probability object){
        array.add(object);
        if(!object.isLocked) ++unlockerNumber;
        calibratePartialLockedValues();
        return;
    }
    //返回长度
    public int size (){
        return array.size();
    }
    //删除元素
    public void remove(int index) {
        checkBounds(index);
        if(!array.get(index).isLocked) --unlockerNumber;
        array.remove(index);
        if(!array.isEmpty()) calibratePartialLockedValues();
        return;
    }
    public void remove(Probability object){
        if(!object.isLocked) --unlockerNumber;
        array.remove(object);
        if(!array.isEmpty()) calibratePartialLockedValues();
        return;
    }
    //转为字符串用于debug输出
    @NonNull
    public String toString(){
        StringBuilder output = new StringBuilder();
        int i = 1;
        for(Probability obj:array){
            String temp = "";
            temp="第 " + i + " 个概率对象: "+obj.toString();
            ++i;
            output.append(temp);
        }
        return output.toString();
    }
    //获得索引对应的对象
    public Probability getObject(int index){
        checkBounds(index);
        return array.get(index);
    }
    //获取对象对应的索引
    public int getIndex(Probability object){
        for(int i = 0;i < array.size();++i){
            if(array.get(i) == object){
                return i;
            }
        }
        return -1;
    }
    //测试主函数
    public static void main (String[] args){
        ProbabilityArray object = new ProbabilityArray();
        for(int i = 0;i < 5;++i){
            object.add();
        }
        System.out.print(object.toString());
        object.setLock(3,true);
        object.remove(object.getObject(2));
        for(int i = 0;i < 5;++i){
            object.add();
        }
        object.setWeight(8,60);
        System.out.print(object.toString());

    }
}
