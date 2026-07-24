package `in`.mato.cryptodetail.domain.repository

import `in`.mato.cryptodetail.domain.model.Crypto
import `in`.mato.cryptodetail.domain.model.CryptoDetail
import `in`.mato.cryptodetail.domain.model.HistoryPoint

interface CryptoRepository {
    suspend fun getTopCryptocurrencies(limit: Int, page: Int = 1): Result<List<Crypto>>
    suspend fun getCryptoDetail(id: String): Result<CryptoDetail>
    suspend fun getPriceHistory(id: String, days: Int): Result<List<HistoryPoint>>
}
