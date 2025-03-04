package ru.prodcontest.booq.domain.usecase

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import ru.prodcontest.booq.domain.model.CoworkingItemModel
import ru.prodcontest.booq.domain.repository.ApiRepository
import ru.prodcontest.booq.domain.util.ResultFlow
import ru.prodcontest.booq.domain.util.ResultWrapper
import javax.inject.Inject

class GetCoworkingDataUseCase @Inject constructor(
    private val apiRepository: ApiRepository
) {
    operator fun invoke(
        buildingId: String,
        coworkingId: String,
        height: Int
    ): ResultFlow<List<CoworkingItemModel>> = flow {
        apiRepository.getItemsOfCompany().onEach { itemsOfCompany ->
            when (itemsOfCompany) {
                is ResultWrapper.Ok -> {
                    apiRepository.getItemsOfCoworking(buildingId, coworkingId)
                        .onEach { itemsOfCoworking ->
                            when (itemsOfCoworking) {
                                is ResultWrapper.Ok -> {
                                    apiRepository.getBookingsOfCoworking(buildingId, coworkingId)
                                        .onEach { bookings ->
                                            when (bookings) {
                                                is ResultWrapper.Ok -> {
                                                    val res =
                                                        itemsOfCoworking.data.map { cowoItem ->
                                                            CoworkingItemModel(
                                                                description = cowoItem.description,
                                                                id = cowoItem.id,
                                                                item = itemsOfCompany.data
                                                                    .first { it.id == cowoItem.itemId }
                                                                    .withFixedOffsets(),
                                                                name = cowoItem.name,
                                                                basePoint = cowoItem.basePoint.rebaseFromBottomLeft(
                                                                    height
                                                                ),
                                                                occupied = bookings.data
                                                                    .filter { it.coworkingItemId == cowoItem.id }
                                                                    .map { it.toBookingSpan() }
                                                            )
                                                        }
                                                    emit(ResultWrapper.Ok(res))
                                                }

                                                is ResultWrapper.Error -> emit(
                                                    ResultWrapper.Error(
                                                        bookings.message,
                                                        bookings.cause
                                                    )
                                                )

                                                ResultWrapper.Loading -> emit(ResultWrapper.Loading)
                                            }
                                        }.collect()
                                }

                                is ResultWrapper.Error -> emit(itemsOfCoworking)
                                ResultWrapper.Loading -> emit(ResultWrapper.Loading)
                            }
                        }.collect()
                }

                is ResultWrapper.Error -> emit(itemsOfCompany)
                ResultWrapper.Loading -> emit(ResultWrapper.Loading)
            }
        }.collect()
    }
}