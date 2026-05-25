import { Module } from '@nestjs/common';
import { UnlocksController } from './unlocks.controller';

@Module({
  controllers: [UnlocksController],
})
export class UnlocksModule {}
