public class Leetcode {
    public static void main(String[] args) {
        System.out.println(rob(new int[]{1, 2, 1, 1}));
    }

    public int waysToStep(int n) {
        
        return 0;
    }


    public static int rob(int[] nums) {
        int dp[] = new int[nums.length];
        int maxrob = 0;
        for (int i = 0; i < nums.length; i++) {
            dp[i] = nums[i];
            for (int j = 0; j < i - 1; j++) {
                int robj = dp[j] + nums[i];
                dp[i] = Math.max(dp[i], robj);
            }
            maxrob = Math.max(maxrob, dp[i]);
        }
        return maxrob;
    }

    public static int maxProfit(int[] prices) {
        int dp[] = new int[prices.length];
        int maxdp = 0;
        dp[0] = 0;
        for (int i = 1; i < prices.length; i++) {
            for (int j = 0; j < i; j++) {
                int aa = prices[i] - prices[j];
                if (aa > 0) {
                    dp[i] = Math.max(dp[i], dp[j] + aa);
                }

            }
            maxdp = Math.max(maxdp, dp[i]);
        }
        return maxdp;
    }

}
