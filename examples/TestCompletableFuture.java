package br.com.guj;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by filipi on 8/8/17.
 */
public class TestCompletableFuture {

    private void executeAsync(CompletableFuture<String> future, String valorPromise, Long timeToComplete){
        CompletableFuture.runAsync(()->{
            try {
                Thread.sleep(timeToComplete);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Execute Async " + valorPromise);
            future.complete(valorPromise);
            //future.completeExceptionally(new RuntimeException("Erro ao executar future"));
        });
    }

    private String printResults(String valor){
        valor = valor + " Alterado";
        System.out.println("printResults " + valor);
        return valor;
    }

    private CompletableFuture createPromise(String valorPromise, Long timeToComplete) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        this.executeAsync(future, valorPromise, timeToComplete);
        return future.thenApply(valor -> printResults(valor));
    }

    private CompletableFuture composedFuture(String valor) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = new CompletableFuture<>();
        this.executeAsync(future, valor + " composed", 1000L);
        return future;
    }

    private CompletableFuture createPromiseAll(List<CompletableFuture> promises){
        CompletableFuture promiseAll = CompletableFuture.allOf(promises.toArray(new CompletableFuture[promises.size()]))
                .thenApply((valor) -> {
                    List<String> valores = new ArrayList<String>();
                    promises.forEach((promise)->{
                        valores.add((String)promise.join());
                    });
                    return valores;
                });
        return promiseAll;
    }

    private CompletableFuture combinePromises(List<CompletableFuture> promises){
        CompletableFuture<List<String>> combinedPromise = CompletableFuture.completedFuture(new ArrayList<String>());
        for (CompletableFuture<String> promise: promises){
            combinedPromise = combinedPromise.thenCombine(promise, (firstResult, secondResult)->{
                firstResult.add(secondResult);
                return firstResult;
            });
        }
        return combinedPromise;
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        TestCompletableFuture test = new TestCompletableFuture();

        List<CompletableFuture> promises = new ArrayList<>();
        for (int i=1; i<=2; i++){
            CompletableFuture promise = test.createPromise("Test" + i, (Long) 1000L * i)
                    .thenCompose((valor) -> {
                        try {
                            return test.composedFuture((String)valor);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    })
            /*.exceptionally((e)->{
                return "Finalizou com erro";
            })*/
                    ;
            promises.add(promise);
        }
        //test.combinePromises(promises)
        test.createPromiseAll(promises)
                .thenAccept((it) -> {
                    System.out.println(it);
                }).get();
    }
}
