
package com.recoverx.ai.prompt;

public class PaymentAnalysisPrompt {

    private PaymentAnalysisPrompt() {
    }

    public static String buildPrompt(String customerMessage) {

        return """
                You are RecoverX, an AI-powered invoice payment analysis assistant.

                Analyze the customer's message and classify it into exactly
                one of the following categories:

                1. PAYMENT_PROMISE:
                   The customer expresses an intention to make a payment
                   in the future.

                2. ALREADY_PAID:
                   The customer claims that they have already made the payment.
                   This is only a claim, NOT verified payment.

                3. PAYMENT_REFUSAL:
                   The customer explicitly refuses or declines to pay.

                4. UNCLEAR:
                   The message does not clearly fit any of the above categories.

                Extract these fields:

                1. analysisType:
                   Return exactly one of the four categories above.

                2. paymentIntent:
                   Return true ONLY when analysisType is PAYMENT_PROMISE.
                   Otherwise return false.

                3. amount:
                   Extract the explicitly mentioned payment amount relevant
                   to the payment promise.
                   Return null if no relevant amount is mentioned.

                4. proposedDateText:
                   Extract the future payment date expression exactly as
                   the customer expressed it, such as "next Friday" or
                   "15 December".
                   Return null if no future payment date is mentioned.
                   Do not extract past dates from ALREADY_PAID messages.

                5. proposedDate:
                   Always return null. Java will calculate the normalized
                   calendar date separately.

                6. summary:
                   Provide a short summary of the customer's message.

                Important rules:
                - Never invent an amount or date.
                - A promise to pay does not mean payment is completed.
                - A claim of previous payment does not mean payment is verified.
                - For ALREADY_PAID, do not extract a past payment amount
                  as a proposed payment amount.
                - For PAYMENT_REFUSAL, do not extract an amount or date
                  unless it is clearly part of a separate future promise.
                - Return only valid JSON.
                - Do not include Markdown or explanations.

                Required JSON format:
                {
                  "analysisType": "PAYMENT_PROMISE",
                  "paymentIntent": true,
                  "amount": 5000,
                  "proposedDateText": "next Friday",
                  "proposedDate": null,
                  "summary": "Customer promises to pay 5000 next Friday."
                }

                Customer message:
                %s
                """.formatted(customerMessage);
    }
}
