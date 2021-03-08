package com.community;

import java.util.Arrays;

public class SortTests {
    public static void main(String[] args) {
        int arr[]={50,90,-8,100,-78,5};
        quickSort(arr,0,arr.length-1);
        System.out.println(Arrays.toString(arr));
    }

    public static void quickSort(int[] arr, int left, int right) {
        int l=left;
        int r=right;
        int pivot=arr[(left+right)/2];
        int temp=0;
        while (l<r){
            while (arr[l]<pivot)
                l++;
            while (arr[r]>pivot)
                r--;
            if(l>=r)
                break;
            temp=arr[l];
            arr[l]=arr[r];
            arr[r]=temp;
            if(arr[l]==pivot)
                r--;
            if(arr[r]==pivot)
                l++;
        }
        if(l==r){
            r--;
            l++;
        }
        if(left<r){
            quickSort(arr,left,r);
        }
        if(right>l){
            quickSort(arr,l,right);
        }
    }
}
